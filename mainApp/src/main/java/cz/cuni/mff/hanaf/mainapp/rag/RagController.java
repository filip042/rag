package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/app")
public class RagController {

    @Autowired
    private FileLoader fileLoader;

    private final Map<String, CompletableFuture<Map<String, Object>>> tasks = new ConcurrentHashMap<>();


    /**
     * Performs a similaritySearch given the query and the documents in the workspace
     *
     * @param query The query being searched for
     * @param workSpace The id of the workspace with the context being searched
     * @param topK The amount of results to display
     * @return A list of documents that are most similar to the query
     */
    @GetMapping("/search")
    public List<Document> search(@RequestParam String query, @RequestParam long workSpace, @RequestParam int topK) {
        return fileLoader.searchSimilarDocuments(query, workSpace, topK);
    }

    /**
     * Passes the given query on to the LLM, which uses context from the given workspace to answer the question
     *
     * @param payload The map containing the query to be passed to the LLM under 'query' and the id of the workspace with the context being used under 'workSpace'
     * @return The answer as a string, followed by comma-delimited sources on the last line // todo
     */
    @PostMapping("/ask")
    public Map<String, Object> search(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.get("query");
        long workSpace = ((Number) payload.get("workSpace")).longValue();
        String taskId = UUID.randomUUID().toString();
        CompletableFuture<Map<String, Object>> future = fileLoader.ask(query, workSpace);
        tasks.put(taskId, future);
        return Map.of("taskId", taskId);
    }

    // todo docstring
    @PostMapping("/answer")
    public ResponseEntity<?> getAskStatus(@RequestBody Map<String, String> payload) {
        String taskId = payload.get("taskId");
        CompletableFuture<Map<String, Object>> future = tasks.get(taskId);
        if (future == null) {
            return ResponseEntity.notFound().build();
        }

        if (future.isDone()) {
            try {
                Map<String, Object> result = future.get();
                tasks.remove(taskId);
                return ResponseEntity.ok(result);
            } catch (java.lang.InterruptedException | java.util.concurrent.ExecutionException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", e.getMessage()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("status", "processing"));
        }
    }

    /**
     * Gets the status of the documents being added to the given workspace
     * @param workSpace The id of the workspace being added toi
     * @return A map containing a list of indexed files under "finishedFiles" and a boolean value for if all files have been indexed under "done"
     */
    @GetMapping("/status")
    public Map<String, Object> getIndexStatus(@RequestParam long workSpace) {
        return fileLoader.allAdded(workSpace);
    }

    /**
     * Adds the documents in the given directory to the given workspace
     *
     * @param payload A map containing the path to the directory and the id of the workspace the documents will be added to
     */
    @PostMapping("/add")
    public void md(@RequestBody Map<String, Object> payload) {
        String path = (String) payload.get("path");
        long workSpace = ((Number) payload.get("workSpace")).longValue();
        fileLoader.addDoc(path, workSpace);
    }

    /**
     * Deletes the workspace set in the payload
     *
     * @param payload The map containing the id of the workspace to be deleted
     */
    @PostMapping("/delete")
    public void deleteDocuments(@RequestBody Map<String, Long> payload) {
        long workSpace = payload.get("workSpace");
        fileLoader.deleteWorkspace(workSpace);
    }
}

