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
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

@Service
public class FileLoader {

    @Autowired
    private VectorStore vectorStore; // maybe map instead of metadata

    @Autowired
    private OpenAiChatModel chatModel;

    private Instant lastModifiedTime = Instant.MIN;

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
                .withAdditionalMetadata("lastReadTime", thisTime.getEpochSecond()) // todo also user, language
                .build();


        if (isDir(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                thisTime = Instant.now();
                Instant finalThisTime = thisTime;
                paths  // todo doesn't add when files have been deleted
                        .filter(Files::isRegularFile).filter(f -> {
                            try {
                                return Files.getLastModifiedTime(f).toInstant().isAfter(lastModifiedTime);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).forEach(f -> {
                            System.out.println(f.toString());
                            ForkJoinPool.commonPool().execute(new ForkJoinLoad(f, workspace, finalThisTime, config, vectorStore, chatModel));
                        });
                lastModifiedTime = Instant.now();
                // todo what's the best way to set flag when the pool is finished
                //todo on frontend
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
