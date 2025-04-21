package com.example.demo;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/add")
    public void addDocuments() {
        documentService.addDocuments();
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query, @RequestParam int topK) {
        return documentService.searchSimilarDocuments(query, topK);
    }

    @GetMapping("/search")
    public void deleteDocument(@RequestParam String id, @RequestParam int topK) {
        documentService.deleteDocument(id);
    }
}

