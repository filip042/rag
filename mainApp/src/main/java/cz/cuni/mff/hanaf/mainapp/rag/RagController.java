package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class RagController {

    @Autowired
    private FileLoader fileLoader;

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query, @RequestParam long workSpace, @RequestParam int topK) {
        return fileLoader.searchSimilarDocuments(query, workSpace, topK);
    }

    @GetMapping("/ask")
    public String search(@RequestParam String query, long workSpace) {
        return fileLoader.ask(query, workSpace);
    }

    @GetMapping("/status")
    public String getIndexStatus(@RequestParam long workSpace) {
        return fileLoader.allAdded(workSpace) ? "true" : "false";
    }

    @PostMapping("/add")
    public void md(@RequestBody Map<String, Object> payload) {
        String path = (String) payload.get("path");
        long workSpace = ((Number) payload.get("workSpace")).longValue();
        fileLoader.addDoc(path, workSpace);
    }

    @PostMapping("/delete")
    public void deleteDocuments(@RequestBody Map<String, Long> payload) {
        long workSpace = payload.get("workSpace");
        fileLoader.deleteWorkspace(workSpace);
    }
}

