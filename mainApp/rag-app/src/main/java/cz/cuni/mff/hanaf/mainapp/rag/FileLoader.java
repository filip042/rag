package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.core.llm.LlmMethods;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileLoader {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ProjectRepository projectRepository;
    private final LlmMethods llmMethods;
    private final Executor llmExecutor;

    public FileLoader(VectorStore vectorStore, ChatModel chatModel, ProjectRepository projectRepository, LlmMethods llmMethods, @Qualifier("llmExecutor") Executor llmExecutor) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.projectRepository = projectRepository;
        this.llmMethods = llmMethods;
        this.llmExecutor = llmExecutor;
    }

    @Value("classpath:prompts/ask-template.txt")
    private Resource askTemplateResource;

    @Value("classpath:prompts/do-not-know-prompt.txt")
    private Resource doNotKnowPromptResource;

    private final Map<Long, CompletableFuture<Void>> indexingTasks = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentLinkedQueue<String>> finishedFiles = new ConcurrentHashMap<>();
    private final Map<Long, List<Path>> allFilesToIndex = new ConcurrentHashMap<>();

    /**
     * Return the given amount of documents most similar to the given query from the given workspace
     *
     * @param query The query being searched for
     * @param workSpace The id of the workspace to search
     * @param topK The number of documents to return
     * @return A list of documents most similar to the query
     */
    public List<Document> searchSimilarDocuments(String query, long workSpace, int topK) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("workSpace", workSpace).build();
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).filterExpression(filterExpression).topK(topK).build());
    }

    /**
     * todo
     * @param query
     * @param workSpace
     * @param progress
     * @return
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress) {
        return ask(query, workSpace, progress, null);
    }

    /**
     * Get an answer to a question from the LLM using the documents in the workspace
     *
     * @param query The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @return The answer as a string, alongside comma-delimited sources on the last line // todo
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress, ChatModel chatModel) {
        if (chatModel == null) {
            chatModel = this.chatModel;
        }
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        return CompletableFuture.supplyAsync(() -> {
            FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
            Filter.Expression filterExpression = expressionBuilder.eq("workSpace", workSpace).build();
            int size = 5;
            progress.put("total", size);
            SearchRequest request = SearchRequest.builder().query(query).filterExpression(filterExpression).topK(size).build();

            System.out.println(query);

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(askTemplateResource);

            AtomicInteger count =  new AtomicInteger(0);
            AtomicBoolean verified = new AtomicBoolean(false);
            progress.put("checked", count);
            progress.put("checked_all", verified);

            System.out.println("blam");
            List<Document> candidates = vectorStore.similaritySearch(request);
            System.out.println("blim");

            List<Document> relevant =
                    candidates.stream()
                            .filter(doc -> {
                                System.out.println("boo");
                                boolean isRelevant = llmMethods.verifySource(doc.getText(), query);
                                count.incrementAndGet();
                                return isRelevant;
                            })
                            .toList();

            verified.set(true);

            VerifyingQuestionAnswerAdvisor qaAdvisor;
            try {
                qaAdvisor = VerifyingQuestionAnswerAdvisor.builder(vectorStore)
                        .promptTemplate(systemPromptTemplate)
                        .doNotKnowPrompt(doNotKnowPromptResource.getContentAsString(StandardCharsets.UTF_8))
                        .searchRequest(request)
                        .documents(relevant)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ChatClientResponse clientResponse = chatClient.prompt(query)
                    .advisors(qaAdvisor)
                    .call()
                    .chatClientResponse();

            String answer = Optional.ofNullable(clientResponse.chatResponse())
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(null);

            List<Document> documents = qaAdvisor.getVerifiedDocuments();

            Set<String> sources = documents.stream()
                    .map(this::extractDocumentName)
                    .collect(Collectors.toSet());

            String formattedAnswer = llmMethods.prepareAnswer(answer);

            progress.put("answer", formattedAnswer);
            progress.put("sources", sources);
            progress.put("documents", documents);
            progress.put("status", "done");
            return null;
        }, llmExecutor);
    }

    // todo
    public void addDocuments(MultipartFile[] files, long workspace) {
        if (files == null || files.length == 0) {
            return;
        }

        try {
            Instant indexingStartTime = Instant.now();
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(workspace, finishedQueue);

            Project project = projectRepository.getReferenceById(workspace);
            Set<String> existingFiles = Optional.ofNullable(project.getFiles()).orElseGet(HashSet::new);
            Instant lastIndexedTime = project.getLastIndexedTime();

            Path tempDir = Files.createTempDirectory("uploads_" + workspace);

            List<Path> toIndex = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Path filePath = tempDir.resolve(file.getOriginalFilename());
                    Files.createDirectories(filePath.getParent());
                    file.transferTo(filePath.toFile());
                    toIndex.add(filePath);
                }
            }

            allFilesToIndex.put(workspace, toIndex);

            Set<String> currentFiles = toIndex.stream()
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());

            Set<String> filesToRemove = existingFiles.stream()
                    .filter(file -> !currentFiles.contains(file))
                    .collect(Collectors.toSet());

            List<CompletableFuture<Void>> futures = toIndex.stream()
                    .filter(f -> {
                        try {
                            if (lastIndexedTime == null) {
                                return true;
                            }
                            String fileName = f.getFileName().toString();
                            if (existingFiles.contains(fileName) && !Files.getLastModifiedTime(f).toInstant().isAfter(lastIndexedTime)) {
                                finishedQueue.add(fileName);
                                return false;
                            }
                            return true;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(f -> CompletableFuture.runAsync(() -> {
                        System.out.println("Processing: " + f);
                        try {
                            DocumentLoader loader = new DocumentLoader(vectorStore, chatModel);
                            loader.load(f, workspace, indexingStartTime);
                            System.out.println("Finished processing: " + f);
                        } catch (Exception e) {
                            System.err.println("Failed processing " + f + ": " + e.getMessage());
                        }
                        finishedQueue.add(f.getFileName().toString());
                    }, llmExecutor))
                    .toList();

            CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            indexingTasks.put(workspace, allDone);

            allDone.thenRun(() -> {
                if (!filesToRemove.isEmpty()) {
                    System.out.println("Removing " + filesToRemove.size() + " deleted files");
                    for (String fileToRemove : filesToRemove) {
                        deleteDocumentsForFile(workspace, fileToRemove);
                    }
                }

                project.setFiles(currentFiles);
                project.setLastIndexedTime(indexingStartTime);
                projectRepository.save(project);

                // Clean up temporary directory
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    System.err.println("Failed to delete temp file: " + path);
                                }
                            });
                } catch (IOException e) {
                    System.err.println("Failed to clean up temp directory: " + e.getMessage());
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteDocumentsForFile(long workspace, String fileName) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.and(
                expressionBuilder.eq("workSpace", workspace),
                expressionBuilder.eq("source", fileName)
        ).build();
        vectorStore.delete(filterExpression);
    }

    /**
     * Returns the status and list of files processed for the given workspace.
     *
     * @param workspace The id of the workspace to check
     * @return A map with two key-value pairs:
     *         - "todo": The number of documents that are being indexed
     *         - "finishedFiles": A list of file paths that have been successfully processed
     */
    public Map<String, Object> allAdded(long workspace) {
        CompletableFuture<Void> future = indexingTasks.get(workspace); // todo doesn't return indexed stuff, only stuff that has been indexed after addDoc was called last
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(workspace);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();
        int total = (allFilesToIndex.get(workspace) != null) ? allFilesToIndex.get(workspace).size() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("todo", total); // todo think these through a bit more
        result.put("finishedFiles", finishedList);

        return result;
    }

    /**
     * Removes all documents in the given workspace from the database
     *
     * @param workspace The id of the workspace that is being deleted
     */
    public void deleteWorkspace(long workspace) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.eq("workSpace", workspace).build();
        vectorStore.delete(filterExpression);
        System.out.println("Deleted workspace " + workspace);
    }

    public void setSystemPrompt(Resource promptResource) {
        this.askTemplateResource = promptResource;
    }

    private String extractDocumentName(Document document) {
        Pattern pattern = Pattern.compile("<chunk source=\"([^\"]+)\">\\s*(.*?)\\s*</chunk>", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(document.getText());
        String fileName = "";
        if (matcher.find()) {
            fileName = matcher.group(1);
        }

        return fileName;
    }
}
