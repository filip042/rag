package cz.cuni.mff.hanaf.langchaindemo;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.web.bind.annotation.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.IOException;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

@RestController
@RequestMapping("/documents")
public class Controller {

    static String MODEL_NAME = "tinyllama"; // try other local ollama model names
    static String BASE_URL = "http://localhost:11434";

    @GetMapping("/test")
    public void addDocuments() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .build();
        String answer = model.chat("List top 10 cites in China");
        System.out.println(answer);
    }

    @GetMapping("/setup")
    public void setupStore() throws IOException {
        RestClient restClient = RestClient
            .builder(HttpHost.create("http://localhost:9200"))
            .build();

        ElasticsearchEmbeddingStore store = ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .build();

        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("hf.co/second-state/All-MiniLM-L6-v2-Embedding-GGUF")
                .build();

        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        store.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        store.add(embedding2, segment2);

        restClient.performRequest(new Request("POST", "/default/_refresh"));

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
        EmbeddingSearchResult<TextSegment> relevant = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .build());
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.matches().getFirst();

        System.out.println(embeddingMatch.score()); // 0.8138435
        System.out.println(embeddingMatch.embedded().text()); // I like football.

        restClient.close();

    }

}

