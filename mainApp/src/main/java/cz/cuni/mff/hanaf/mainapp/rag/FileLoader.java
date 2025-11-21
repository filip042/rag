package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.llm.LlmMethods;
//import cz.cuni.mff.hanaf.mainapp.llm.OllamaConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileLoader {

    private final VectorStore vectorStore; // maybe map instead of metadata
    private final OllamaChatModel chatModel;
    private final ProjectRepository projectRepository;
    private final LlmMethods llmMethods;
    private final Executor llmExecutor;

    public FileLoader(VectorStore vectorStore, OllamaChatModel chatModel, ProjectRepository projectRepository, LlmMethods llmMethods, @Qualifier("llmExecutor") Executor llmExecutor) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.projectRepository = projectRepository;
        this.llmMethods = llmMethods;
        this.llmExecutor = llmExecutor;
    }

    @Value("classpath:prompts/ask-template.txt")
    private Resource askTemplateResource;

    private Instant lastModifiedTime = Instant.MIN;
    private final Map<Long, CompletableFuture<Void>> indexingTasks = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentLinkedQueue<String>> finishedFiles = new ConcurrentHashMap<>();
    private final Map<Long, List<Path>> allFilesToIndex = new ConcurrentHashMap<>();

    /**
     * Checks if a given path is a directory
     *
     * @param path The path to check
     * @return True if the path is a directory, false otherwise
     */
    private Boolean isDir(Path path) { // todo move to own class
        if (path == null || !Files.exists(path)) return false;
        else return Files.isDirectory(path);
    }

    /**
     * Return the given amount of documents most similar to the qiven query from the given workspace
     *
     * @param query The query being searched for
     * @param workSpace The id of the workspace to search
     * @param topK The number of documents to return
     * @return A list of documents most similar to the query
     */
    public List<Document> searchSimilarDocuments(String query, long workSpace, int topK) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(workSpace)
        );
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).filterExpression(filterExpression).topK(topK).build());
    }

    /**
     * Get an answer to a question from the LLM using the documents in the workspace
     *
     * @param query The query to be answered
     * @param workSpace The id of the workspace with the source documents
     * @return The answer as a string, alongside comma-delimited sources on the last line // todo
     */
    public CompletableFuture<Void> ask(String query, long workSpace, Map<String, Object> progress) {
        return CompletableFuture.supplyAsync(() -> {
            Filter.Expression filterExpression = new Filter.Expression(
                    Filter.ExpressionType.EQ,
                    new Filter.Key("workSpace"),
                    new Filter.Value(workSpace)
            );
            int size = 5;
            progress.put("total", size);
            SearchRequest request = SearchRequest.builder().filterExpression(filterExpression).topK(size).build();
            ChatClient chatClient = ChatClient.builder(chatModel).build();

            System.out.println(query);

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(askTemplateResource);

            AtomicInteger count =  new AtomicInteger(0);
            AtomicBoolean verified = new AtomicBoolean(false);
            progress.put("checked", count);
            progress.put("checked_all", verified);

            VerifyingQuestionAnswerAdvisor qaAdvisor = VerifyingQuestionAnswerAdvisor.builder(vectorStore)
                    .promptTemplate(systemPromptTemplate)
                    .searchRequest(request)
                    .llmMethods(llmMethods)
                    .counter(count)
                    .verified(verified)
                    .build();

            ChatClientResponse clientResponse = chatClient.prompt(query)
                    .advisors(qaAdvisor)
                    .call().chatClientResponse();

            String answer = Optional.ofNullable(clientResponse.chatResponse())
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(null);

            List<Document> documents = qaAdvisor.getVerifiedDocuments();

            Set<String> sources = documents.stream()
                    .map(this::extractDocumentName)
                    .collect(Collectors.toSet());

//            Set<String> sources2 = extractSources(clientResponse);
//            for(String source : sources2) {
//                System.out.println(source);
//            }

            Map<String, Object> structuredAnswer = llmMethods.prepareAnswer(answer); // todo
            progress.put("answer", structuredAnswer.get("answer"));
            progress.put("sources", sources);
            progress.put("documents", documents);
            progress.put("status", "done");
            return null;
        }, llmExecutor);
    }

    /**
     * Extract all source document names from the given chat response
     *
     * @param response The ChatClientResponse to extract the source documents from
     * @return A set of all the source document names
     */
    private Set<String> extractSources(ChatClientResponse response) { // todo check against list in answer
        Object documentsObj = response.context().get("qa_retrieved_documents");
        if (documentsObj instanceof List) {
            return ((List<?>) documentsObj).stream()
                    .filter(doc -> doc instanceof Map)
                    .map(doc -> extractSourceFromDocument((Map<?, ?>) doc))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Gets the source metadata value from a document
     *
     * @param document The document to extract the source from
     * @return The source document name as a string
     */
    private String extractSourceFromDocument(Map<?, ?> document) {
        Object metadataObj = document.get("metadata");
        if (metadataObj instanceof Map) {
            Object sourceObj = ((Map<?, ?>) metadataObj).get("source");
            return sourceObj instanceof String ? (String) sourceObj : null;
        }
        return null;
    }

    /**
     * Adds the documents in the given directory to the database
     *
     * @param path The path to the directory with the documents as a string
     * @param workspace The id of the workspace the documents are being added to as a long
     */
    public void addDoc(String path, long workspace) { // doesn't remove files that don't exist
        System.out.println(path);
        Path directory = Path.of(path);

        if (!isDir(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            Instant finalThisTime = Instant.now();
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(workspace, finishedQueue);

            Project project = projectRepository.getReferenceById(workspace);
            Set<String> existingFiles = Optional.ofNullable(project.getFiles()).orElseGet(HashSet::new);

            List<Path> toIndex = paths.filter(Files::isRegularFile).toList();
            allFilesToIndex.put(workspace, toIndex);

            List<CompletableFuture<Void>> futures = toIndex.stream()
                    .filter(f -> {
                        try {
                            if (existingFiles.contains(f.toString()) && !Files.getLastModifiedTime(f).toInstant().isAfter(lastModifiedTime)) {
                                finishedQueue.add(f.toString());
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
                    DocumentLoader loader = new DocumentLoader(f, workspace, finalThisTime, vectorStore, chatModel);
                    loader.load();
                    System.out.println("Finished processing: " + f);
                } catch (Exception e) {
                    System.err.println("Failed processing " + f + ": " + e.getMessage());
                }
                finishedQueue.add(f.toString());
            }))
                    .toList();

            CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            indexingTasks.put(workspace, allDone);
            allDone.thenRun(() -> {
                project.addFiles(finishedQueue);
                projectRepository.save(project);
            });
            lastModifiedTime = finalThisTime;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(workspace)
        );
        vectorStore.delete(filterExpression);
        System.out.println("Deleted workspace " + workspace);
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
