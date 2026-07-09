package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.rag.dto.*;
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
     * Passes the query in the request on to the LLM, which uses context from the given project to answer the question.
     *
     * @param askRequest an {@link AskRequest} record
     * @return an {@link AskResponse} record containing a generated task id, which can be polled via /answer
     */
    @PostMapping("/ask")
    public AskResponse search(@RequestBody AskRequest askRequest) {
        String query = askRequest.query();
        long projectId = askRequest.project();
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
        return new AskResponse(taskId);
    }

    /**
     * Polls the status of an async "ask" task previously initiated by {@link #search}.
     *
     * @param answerRequest an {@link AnswerRequest} record
     * @return 200 OK with the full progress map (and removes the task) if the task is done,
     *         202 Accepted with the current progress map if still in progress,
     *         or 404 Not Found if no task with the given id exists
     */
    @PostMapping("/answer")
    public ResponseEntity<?> getAskStatus(@RequestBody AnswerRequest answerRequest) {
        String taskId = answerRequest.taskId();
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
     * @return an {@link IndexStatusResponse} record
     */
    @GetMapping("/status")
    public IndexStatusResponse getIndexStatus(@RequestParam long project) {
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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Deletes all documents indexed under the given project.
     *
     * @param deleteProjectRequest a {@link DeleteProjectRequest} record
     */
    @PostMapping("/delete")
    public void deleteDocuments(@RequestBody DeleteProjectRequest deleteProjectRequest) {
        long projectId = deleteProjectRequest.project();
        ragService.deleteProject(projectId);
    }
}

