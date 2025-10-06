package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
//import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileLoader {

    @Autowired
    private VectorStore vectorStore; // maybe map instead of metadata

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ProjectRepository projectRepository;

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

    public List<Document> searchSimilarDocuments(String query, long workSpace, int topK) {
        OpenAiChatOptions options = (OpenAiChatOptions) chatModel.getDefaultOptions();
        options.getHttpHeaders().keySet().forEach(System.out::println);
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
     * @return The answer as a string, alongside comma-delimited sources on the last line
     */
    public String ask(String query, long workSpace) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(workSpace)
        );
        SearchRequest request = SearchRequest.builder().filterExpression(filterExpression).topK(10).build();
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        System.out.println(query);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(askTemplateResource);

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(systemPromptTemplate)
                .searchRequest(request)
                .build();

        ChatClientResponse clientResponse = chatClient.prompt(query)
                .advisors(qaAdvisor)
                .call().chatClientResponse();

        String answer = Optional.ofNullable(clientResponse.chatResponse())
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElse( null);

        return answer;
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
                    finishedQueue.add(f.toString());
                    System.out.println("Finished processing: " + f);
                } catch (Exception e) {
                    System.err.println("Failed processing " + f + ": " + e.getMessage());
                }
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
     *         - "done": A boolean indicating whether all indexing tasks for the workspace are complete, false if no indexing took place since the app was started
     *         - "finishedFiles": A list of file paths that have been successfully processed
     */
    public Map<String, Object> allAdded(long workspace) { // todo add endpoint
        CompletableFuture<Void> future = indexingTasks.get(workspace); // todo doesn't return indexed stuff, only stuff that has been indexed after addDoc was called last
        boolean done = (future != null && future.isDone());
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(workspace);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();
        int total = (allFilesToIndex.get(workspace) != null) ? allFilesToIndex.get(workspace).size() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("done", done);
        result.put("todo", total);
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

    /**
     * A temporary method for testing the LLM's no_think mode
     */
    public void testNoThink() {
        OpenAiChatOptions options = new OpenAiChatOptions();
        options.setModel("qwen3");
        options.setTemperature(0.3);
        Prompt prompt = new Prompt("Who is Jon Snow? Be as brief as possible. /no_think", options);

        System.out.println(((chatModel.call(prompt).getResult().getOutput().getText())));
    }
}
