package com.example.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OllamaChatModel chatModel;

    public void addDocuments() {
        List<Document> documents = List.of(
                new Document("The sky is green.")
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
        String userText = "Tell me a joke";

        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String responseContent = chatClient.prompt()
                .user("What is the sky color?")
                .advisors(questionAnswerAdvisor)
                .call()
                .content();

        return responseContent;


//        ChatResponse response = ChatClient.builder(chatModel)
//                .build().prompt()
//                .advisors(new QuestionAnswerAdvisor(vectorStore))
//                .user(userText)
//                .call()
//                .chatResponse();
//
//        assert response != null;
//        return response.getResult().toString();
    }
}

