package com.example.demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private VectorStore vectorStore;

    public void addDocuments() {
        List<Document> documents = List.of(
                new Document("Hi")
        );

        vectorStore.add(documents);
    }

    public List<Document> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
    }

    public void deleteDocument(String documentId) {
        vectorStore.delete(documentId);
    }
}

