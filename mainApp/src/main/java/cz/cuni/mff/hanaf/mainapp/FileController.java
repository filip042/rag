package cz.cuni.mff.hanaf.mainapp;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/app")
public class FileController {

    @Autowired
    private FileLoader fileLoader;

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query, @RequestParam String workSpace, @RequestParam int topK) {
        return fileLoader.searchSimilarDocuments(query, workSpace, topK);
    }

    @GetMapping("/ask")
    public String search(@RequestParam String query, String workSpace) {
        return fileLoader.ask(query, workSpace);
    }

    @GetMapping("/add")
    public void md(@RequestParam String workSpace) {
        fileLoader.addDoc(workSpace);
    }

    @GetMapping("/delete")
    public void deleteDocuments(@RequestParam String name) {
        fileLoader.deleteWorkspace(name);
    }
}

