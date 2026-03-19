package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.*;
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
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Stream;

@Service
public class FileLoader {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ProjectRepository projectRepository;
    private final QuestionRepository questionRepository;
    private final LlmMethods llmMethods;
    private final Executor llmExecutor;

    public FileLoader(VectorStore vectorStore, ChatModel chatModel, ProjectRepository projectRepository, QuestionRepository questionRepository, LlmMethods llmMethods, @Qualifier("llmExecutor") Executor llmExecutor) {
        this.vectorStore = new SynchronizedVectorStore(vectorStore);
        this.chatModel = chatModel;
        this.projectRepository = projectRepository;
        this.questionRepository = questionRepository;
        this.llmMethods = llmMethods;
        this.llmExecutor = llmExecutor;
    }

    @Value("classpath:prompts/ask-template.txt")
    private Resource askTemplateResource;

    @Value("classpath:prompts/do-not-know-prompt.txt")
    private Resource doNotKnowPromptResource;

    private final Map<Long, ConcurrentLinkedQueue<String>> finishedFiles = new ConcurrentHashMap<>();
    private final Map<Long, List<Path>> allFilesToIndex = new ConcurrentHashMap<>();

    /**
     * Get an answer to a question from the LLM using the documents in the workspace,
     * using the default chat model. See {@link #ask(String, long, Map, ChatModel)} for full details.
     *
     * @param query     The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @param progress  The map to be updated with task progress and results
     * @return A CompletableFuture that completes when the answer has been written to {@code progress}
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress) {
        return ask(query, workSpace, progress, null);
    }

    /**
     * Get an answer to a question from the LLM using the documents in the workspace.
     * Results are written into {@code progress} under "answer", "sources", "documents",
     * and "status" (set to "done" on completion).
     *
     * @param query     The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @param progress  The map to be updated with task progress and results
     * @param chatModel The chat model to use, or null to use the default
     * @return A CompletableFuture that completes when the answer has been written to {@code progress}
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

//            System.out.println("blam");
            List<Document> candidates = vectorStore.similaritySearch(request);
//            System.out.println("blim");

            List<Document> relevant =
                    candidates.stream()
                            .filter(doc -> {
                                System.out.println(doc.getMetadata().get("source"));
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

            System.out.println(formattedAnswer);

            projectRepository.findById(workSpace).ifPresent(project -> {
                Question question = new Question();
                question.setQuestion(query);
                question.setAnswer(formattedAnswer);
                question.setAnswerTime(Instant.now());
                question.setSources(sources);
                question.setProject(project);
                questionRepository.save(question);
            });

            return null;
        }, llmExecutor);
    }

    /**
     * Asynchronously indexes the given files into the given workspace.
     * Files whose hash matches an already-indexed version are skipped; files
     * whose hash has changed are re-indexed. Cleans up temporary storage once done.
     *
     * @param files     The files to be indexed
     * @param workspace The id of the workspace to add the files to
     */
    public void addDocuments(MultipartFile[] files, long workspace) {
        if (files == null || files.length == 0) {
            return;
        }

        try {
            Instant indexingStartTime = Instant.now();
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(workspace, finishedQueue);

            Project project = projectRepository.getReferenceById(workspace);
            Map<String, FileInfo> existingFiles = new ConcurrentHashMap<>(Optional.ofNullable(project.getFiles()).orElse(Map.of()));

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

            Set<String> filesToRemove = toIndex.stream()
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        if (!existingFiles.containsKey(fileName)) {
                            return false;
                        }
                        try {
                            String newHash = computeHash(f);
                            String storedHash = existingFiles.get(fileName).getHash();
                            return !newHash.equals(storedHash);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(f -> existingFiles.get(f.getFileName().toString()).getFileId())
                    .collect(Collectors.toSet());

            if (!filesToRemove.isEmpty()) {
                System.out.println("Removing old versions of " + filesToRemove.size() + " files");
                for (String fileToRemove : filesToRemove) {
                    deleteDocumentsForFile(workspace, fileToRemove);
                }
            }

            List<CompletableFuture<Void>> futures = toIndex.stream()
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        try {
                            String newHash = computeHash(f);
                            if (existingFiles.containsKey(fileName) && newHash.equals(existingFiles.get(fileName).getHash())) {
                                finishedQueue.add(fileName);
                                return false;
                            }
                            return true;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(f -> CompletableFuture.runAsync(() -> {
                        String fileName = f.getFileName().toString();
                        String fileId;
                        if (existingFiles.containsKey(fileName)) {
                            fileId = existingFiles.get(fileName).getFileId();
                        } else {
                            fileId = UUID.randomUUID().toString();
                        }
                        try {
                            DocumentLoader loader = new DocumentLoader(vectorStore, chatModel);
                            loader.load(f, workspace, indexingStartTime, fileId);
                            System.out.println("Finished processing: " + f);
                        } catch (Exception e) {
                            System.err.println("Failed processing " + f + ": " + e.getMessage());
                        }

                        try {
                            String hash = computeHash(f);
                            existingFiles.put(fileName, new FileInfo(fileId, hash));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        finishedQueue.add(fileName);
                    }, llmExecutor))
                    .toList();

            CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            allDone.thenRun(() -> {
                Map<String, FileInfo> updatedFiles = new HashMap<>(existingFiles);
                project.setFiles(updatedFiles);
                projectRepository.save(project);

                // Clean up temporary directory
                try (Stream<Path> paths = Files.walk(tempDir)) {
                    paths.sorted(Comparator.reverseOrder())
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

    /**
     * Deletes the chunks in the given workspace from the given file.
     * Spring AI's built-in delete method for Filter Expressions does text search instead of exact-match, leading to false positives, e.g., E._R._Eddison.txt being returned for J._R._R._Tolkien.txt
     * Deleting by UUID bypasses this
     * @param workspace The workspace to delete from
     * @param fileId The UUID of the file whose chunks should be deleted
     */
    private void deleteDocumentsForFile(long workspace, String fileId) {
        FilterExpressionBuilder expressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = expressionBuilder.and(
                expressionBuilder.eq("workSpace", workspace),
                expressionBuilder.eq("fileId", fileId)
        ).build();
        vectorStore.delete(filterExpression);
    }

    /**
     * Returns the status and list of files processed for the given workspace.
     *
     * @param workspace The id of the workspace to check
     * @return A map with two key-value pairs:
     *         - "totalFiles": The number of documents that are being indexed
     *         - "finishedFiles": A list of file paths that have been successfully processed
     */
    public Map<String, Object> allAdded(long workspace) {
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(workspace);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();
        int total = (allFilesToIndex.get(workspace) != null) ? allFilesToIndex.get(workspace).size() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalFiles", total);
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

    /**
     * Overrides the system prompt template used when answering questions.
     * Intended for use in tests.
     *
     * @param promptResource The resource containing the new prompt template
     */
    public void setSystemPrompt(Resource promptResource) {
        this.askTemplateResource = promptResource;
    }

    private String extractDocumentName(Document document) {
        Pattern pattern = Pattern.compile("<chunk source=\"([^\"]+)\">\\s*(.*?)\\s*</chunk>", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(Objects.requireNonNullElse(document.getText(), ""));        String fileName = "";
        if (matcher.find()) {
            fileName = matcher.group(1);
        }

        return fileName;
    }

    private String computeHash(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return DigestUtils.md5DigestAsHex(is);
        }
    }
}
