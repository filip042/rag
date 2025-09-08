package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
//import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Service
public class FileLoader {

    @Autowired
    private VectorStore vectorStore; // maybe map instead of metadata

    @Autowired
    private OpenAiChatModel chatModel;

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
        SearchRequest request = SearchRequest.builder().filterExpression(filterExpression).topK(6).build();
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        PromptTemplate customPromptTemplate =
                PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            Context information is below.

			---------------------
			<question_answer_context>
			---------------------

			Given the context information and no prior knowledge, answer the query.

			Follow these rules:
               
            1. If the answer is not present in the context or if the context is empty, respond with "I don't know" and nothing else.
            2. Do not use phrases like "Based on the context..." or "The provided information...".
            3. Only use relevant information from the context that directly pertains to the question.
            4. Provide a succinct answer without adding any extraneous information.
            5. Do not include reasoning in your response.
            6. Every answer must either begin with a quote from the relevant context or be "I don't know". No other responses are acceptable.
        
           \s""")
                .build();
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(request) // todo also return these
                .promptTemplate(customPromptTemplate)
                .build();


        String responseContent = chatClient.prompt()
                .user(query)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        return responseContent;
    }

    public void addDoc(String path, long workspace) { // todo make return boolean
        System.out.println(path);
        Path directory = Path.of(URI.create("file:///C:/Users/filip/Java/2025-hana/mainApp/testDocuments")); // testing: "file:///C:/Users/filip/IdeaProjects/2025-hana/mainApp"
        Instant thisTime = Instant.now();


        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("workSpace", workspace)
                .withAdditionalMetadata("lastReadTime", thisTime.getEpochSecond()) // todo also language
                .build();


        if (!isDir(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            thisTime = Instant.now();
            Instant finalThisTime = thisTime;
            ConcurrentLinkedQueue<String> finishedQueue = new ConcurrentLinkedQueue<>();
            finishedFiles.put(workspace, finishedQueue);

            List<CompletableFuture<Void>> futures = paths
                    .filter(Files::isRegularFile).filter(f -> {
                        try {
                            return Files.getLastModifiedTime(f).toInstant().isAfter(lastModifiedTime);
                            // todo also check if file is already indexed, if not, index anyway
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(f -> {
                        System.out.println("Submitting: " + f);
                        ForkJoinLoad task = new ForkJoinLoad(f, workspace, finalThisTime, config, vectorStore, chatModel);
                        return CompletableFuture.runAsync(() -> {
                            System.out.println("Running task for: " + f);
                            try {
                                ForkJoinPool.commonPool().invoke(task);
                                finishedQueue.add(f.toString());
                                System.out.println("Finished task for: " + f);
                            } catch (Exception e) {
                                System.err.println("Failed processing " + f + ": " + e.getMessage());
                            }
                        });
                    })
                    .toList();

            CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            indexingTasks.put(workspace, allDone);
            lastModifiedTime = Instant.now();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean allAdded(long workspace) { // todo add endpoint
        CompletableFuture<Void> future = indexingTasks.get(workspace);
        System.out.println(indexingTasks.keySet());
        System.out.println(indexingTasks.values());
        return future != null && future.isDone(); // todo also return finished files
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
}
