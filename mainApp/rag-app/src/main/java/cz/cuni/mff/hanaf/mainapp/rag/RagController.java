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

/**
 * REST controller handling document indexing and RAG query endpoints.
 */
@RestController
@RequestMapping("/app")
public class RagController {

    @Autowired
    private RagService ragService;

    private final Map<String, Map<String, Object>> taskProgress = new ConcurrentHashMap<>();

    /**
     * Passes the given query on to the LLM, which uses context from the given project to answer the question.
     *
     * @param payload a map containing the query under "query" and the project id under "project"
     * @return a map containing a generated task id under "taskId", which can be polled via /answer
     */
    @PostMapping("/ask")
    public Map<String, Object> search(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.get("query");
        long projectId = ((Number) payload.get("project")).longValue();
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> progress = new ConcurrentHashMap<>();
        progress.put("status", "checking");
        progress.put("checked", 0);
        progress.put("total", 0);
        progress.put("checked_all", false);
        progress.put("answer", "");
        progress.put("sources", new ArrayList<String>());
        taskProgress.put(taskId, progress);
        ragService.ask(query, projectId, progress);
        return Map.of("taskId", taskId);
    }

    /**
     * Polls the status of an async "ask" task previously initiated by {@link #search}.
     *
     * @param payload a map containing the task identifier under "taskId"
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
     * Gets the status of the documents being added to the given project.
     *
     * @param project the id of the project being added to
     * @return a map containing the total number of files under "totalFiles" and a list of file that have finished indexing names under "finishedFiles"
     */
    @GetMapping("/status")
    public Map<String, Object> getIndexStatus(@RequestParam long project) {
        return ragService.allAdded(project);
    }

    /**
     * Uploads one or more files and begins asynchronously indexing them into the given project.
     *
     * @param files the files to be indexed
     * @param projectId the id of the project the files should be added to
     * @return 200 OK with "Upload started" if the process was successfully initiated,
     *         or 500 Internal Server Error with an error message if an exception occurred
     */
    @PostMapping("/add")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
            @RequestParam("project") long projectId) {
        try {
            ragService.addDocuments(files, projectId);
            return ResponseEntity.ok("Upload started");
        } catch (Exception e) { // todo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Deletes all documents indexed under the given project.
     *
     * @param payload a map containing the id of the project to be deleted under "project"
     */
    @PostMapping("/delete")
    public void deleteDocuments(@RequestBody Map<String, Long> payload) {
        long projectId = payload.get("project");
        ragService.deleteProject(projectId);
    }
}

