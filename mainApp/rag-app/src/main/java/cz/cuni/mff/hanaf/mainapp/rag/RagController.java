package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/app")
public class RagController {

    @Autowired
    private FileLoader fileLoader;

    private final Map<String, Map<String, Object>> taskProgress = new ConcurrentHashMap<>();

    /**
     * Passes the given query on to the LLM, which uses context from the given workspace to answer the question
     *
     * @param payload The map containing the query to be passed to the LLM under 'query' and the id of the workspace with the context being used under 'workSpace'
     * @return A map containing a generated task id under "taskId", which can be polled via /answer
     */
    @PostMapping("/ask")
    public Map<String, Object> search(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.get("query");
        long workSpace = ((Number) payload.get("workSpace")).longValue();
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> progress = new ConcurrentHashMap<>();
        progress.put("status", "checking");
        progress.put("checked", 0);
        progress.put("total", 0);
        progress.put("checked_all", false);
        progress.put("answer", "");
        progress.put("sources", new ArrayList<String>());
        taskProgress.put(taskId, progress);
        fileLoader.ask(query, workSpace, progress);
        return Map.of("taskId", taskId);
    }

    /**
     * Polls the status of an async "ask" task previously initiated by {@link #search}.
     *
     * @param payload A map containing the task identifier under "taskId"
     * @return 200 OK with the full progress map (and removes the task) if the task is done,
     *         202 Accepted with the current progress map if still in progress,
     *         or 404 Not Found if no task with the given id exists
     */
    @PostMapping("/answer")
    public ResponseEntity<?> getAskStatus(@RequestBody Map<String, String> payload) {
        String taskId = payload.get("taskId");
        Map<String, Object> progress = taskProgress.get(taskId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        String status = (String) progress.get("status");
        if (status.equals("done")) {
            taskProgress.remove(taskId);
            return ResponseEntity.ok(progress);
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(progress);
        }
    }

    /**
     * Gets the status of the documents being added to the given workspace
     * @param workSpace The id of the workspace being added to
     * @return A map containing a list of indexed files under "finishedFiles" and a boolean value for if all files have been indexed under "done"
     */
    @GetMapping("/status")
    public Map<String, Object> getIndexStatus(@RequestParam long workSpace) {
        return fileLoader.allAdded(workSpace);
    }

    /**
     * Uploads one or more files and begins asynchronously indexing them into the given workspace.
     *
     * @param files     The files to be indexed
     * @param workSpace The id of the workspace the files should be added to
     * @return 200 OK with "Upload started" if the process was successfully initiated,
     *         or 500 Internal Server Error with an error message if an exception occurred
     */
    @PostMapping("/add")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
            @RequestParam("workSpace") long workSpace) {
        try {
            fileLoader.addDocuments(files, workSpace);
            return ResponseEntity.ok("Upload started");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
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

