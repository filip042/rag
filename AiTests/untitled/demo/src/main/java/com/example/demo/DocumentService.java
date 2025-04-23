package com.example.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OllamaChatModel chatModel;

    @Autowired
    private ResourceLoader resourceLoader; // use FileSystemLoader

    public Resource loadMarkdownAsResource(String fileName) {
        return resourceLoader.getResource("classpath:" + fileName);
    }

    public void addDocuments() {
        List<Document> documents = List.of(
                new Document("Hi."),
                new Document("The grass is green.")
        );

        vectorStore.add(documents);
    }

    public List<Document> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    public void deleteDocument(String documentId) {
        List<String> tempList = new ArrayList<>();
        tempList.add(documentId);
        vectorStore.delete(tempList);
        System.out.println("Deleted " + documentId);
    }

    public String resource() {
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String responseContent = chatClient.prompt()
                .user("What is the sky color?")
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        return responseContent;
    }

    public void addMd() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .build();

        MarkdownDocumentReader reader = new MarkdownDocumentReader(loadMarkdownAsResource("test.md"), config);
        vectorStore.add(reader.get());
    }
}

