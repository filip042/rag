package cz.cuni.mff.hanaf.mainapp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Service
public class FileLoader {

    @Autowired
    private VectorStore vectorStore; // maybe map instead of metadata

    @Autowired
    private OllamaChatModel chatModel;

    private Instant lastModifiedTime = Instant.MIN;

    private final List<String> formats = new ArrayList<>(List.of(".txt", ".html", ".pdf"));

    private Boolean isDir(Path path) { // todo move to own class
        if (path == null || !Files.exists(path)) return false;
        else return Files.isDirectory(path);
    }

    public List<Document> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    public String ask(String query, String workSpace) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(workSpace)
        );
        SearchRequest request = SearchRequest.builder().filterExpression(filterExpression).build();
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore, request);
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String responseContent = chatClient.prompt()
                .user(query)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        return responseContent;

        // see var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        //        .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
        //        .build();
    }

    public void addDoc(String path) {
        System.out.println(path);
        Path directory = Path.of(URI.create("file:///C:/Users/filip/IdeaProjects/2025-hana/mainApp")); // todo for testing
        Instant thisTime = Instant.now();


        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("workSpace", path) // todo temp
                .withAdditionalMetadata("lastReadTime", thisTime.getEpochSecond()) // todo also user, language
                .build();


        if (isDir(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                thisTime = Instant.now();
                Instant finalThisTime = thisTime;
                paths
                        .filter(Files::isRegularFile).filter(f -> {
                            try {
                                return Files.getLastModifiedTime(f).toInstant().isAfter(lastModifiedTime);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).forEach(f -> {
                            ForkJoinPool.commonPool().execute(new ForkJoinLoad(f, path, finalThisTime, config, vectorStore));
                        });
                lastModifiedTime = Instant.now();
                // todo wait for finish
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteWorkspace(String fileName) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("workSpace"),
                new Filter.Value(fileName)
        );
        vectorStore.delete(filterExpression);
        System.out.println("Deleted " + fileName);
    }
}
