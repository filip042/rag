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
import java.net.URI;
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

    private Set<String> extractSources(ChatClientResponse response) { // todo probably unnecessary
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

    private String extractSourceFromDocument(Map<?, ?> document) {
        Object metadataObj = document.get("metadata");
        if (metadataObj instanceof Map) {
            Object sourceObj = ((Map<?, ?>) metadataObj).get("source");
            return sourceObj instanceof String ? (String) sourceObj : null;
        }
        return null;
    }

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

            Set<String> finishedFilesSet = ConcurrentHashMap.newKeySet();

            List<CompletableFuture<Void>> futures = paths
                    .filter(Files::isRegularFile).filter(f -> {
                        try {
                            if (existingFiles.contains(f.toString()) && !Files.getLastModifiedTime(f).toInstant().isAfter(lastModifiedTime)) {
                                finishedFilesSet.add(f.toString());
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
                    finishedFilesSet.add(f.toString());
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
                project.addFiles(finishedFilesSet);
                projectRepository.save(project);
            });
            lastModifiedTime = finalThisTime;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> allAdded(long workspace) { // todo add endpoint
        CompletableFuture<Void> future = indexingTasks.get(workspace);
        boolean done = (future != null && future.isDone());
        ConcurrentLinkedQueue<String> finishedQueue = finishedFiles.get(workspace);
        List<String> finishedList = finishedQueue != null ? new ArrayList<>(finishedQueue) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("done", done);
        result.put("finishedFiles", finishedList);

        return result;
    }

    public void deleteWorkspace(long fileName) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(fileName)
        );
        vectorStore.delete(filterExpression);
        System.out.println("Deleted " + fileName);
    }

    public void testNoThink() {
        OpenAiChatOptions options = new OpenAiChatOptions();
        options.setModel("qwen3");
        options.setTemperature(0.3);
        Prompt prompt = new Prompt("Who is Jon Snow? Be as brief as possible. /no_think", options);

        System.out.println(((chatModel.call(prompt).getResult().getOutput().getText())));
    }
}
