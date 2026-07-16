package cz.cuni.mff.hanaf.mainapp.rag;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.rag.dto.*;
import cz.cuni.mff.hanaf.mainapp.security.AccessGuard;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller handling document indexing and RAG query endpoints.
 */
@RestController
@RequestMapping("/app")
public class RagController {


    private final RagService ragService;
    private final ProjectRepository projectRepository;
    private final AccessGuard accessGuard;

    private final Map<String, Map<String, Object>> taskProgress = new ConcurrentHashMap<>();

    @Autowired
    RagController(RagService ragService, ProjectRepository projectRepository, AccessGuard accessGuard) {
        this.ragService = ragService;
        this.projectRepository = projectRepository;
        this.accessGuard = accessGuard;
    }

    private Project requireAccess(long projectId, HttpSession session) {
        User user = accessGuard.currentUser(session);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!accessGuard.hasAccess(user, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return project;
    }

    private Project requireAdmin(long projectId, HttpSession session) {
        Project project = requireAccess(projectId, session);
        User user = accessGuard.currentUser(session);
        if (!accessGuard.isAdmin(user, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return project;
    }

    private Object userKey(User user, HttpSession session) {
        return user.getId() != null ? user.getId() : session.getId();
    }

    /**
     * Passes the query in the request on to the LLM, which uses context from the given project to answer the question.
     * The caller must be authenticated and have access to the given project.
     *
     * @param askRequest an {@link AskRequest} record
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return an {@link AskResponse} record containing a generated task id, which can be polled via /answer
     * @throws org.springframework.web.server.ResponseStatusException 401 UNAUTHORIZED if no user is authenticated in
     *         the session, 404 NOT_FOUND if the project does not exist, or 403 FORBIDDEN if the user does not have
     *         access to the project
     */
    @PostMapping("/ask")
    public AskResponse search(@RequestBody AskRequest askRequest, HttpSession session) {
        requireAccess(askRequest.project(), session);
        User user = accessGuard.currentUser(session);

        String taskId = UUID.randomUUID().toString();
        Map<String, Object> progress = new ConcurrentHashMap<>();
        progress.put("status", "checking");
        progress.put("checked", 0);
        progress.put("total", 0);
        progress.put("checked_all", false);
        progress.put("answer", "");
        progress.put("sources", new ArrayList<String>());
        progress.put("userId", userKey(user, session));
        taskProgress.put(taskId, progress);        ragService.ask(askRequest.query(), askRequest.project(), progress);
        return new AskResponse(taskId);
    }

    /**
     * Polls the status of an async "ask" task previously initiated by {@link #search}.
     * Only the user who created the task may poll it.
     *
     * @param answerRequest an {@link AnswerRequest} record
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return 200 OK with the full progress map (and removes the task) if the task is done or an error occured,
     *         202 Accepted with the current progress map if still in progress,
     *         or 404 Not Found if no task with the given id exists
     * @throws org.springframework.web.server.ResponseStatusException 401 UNAUTHORIZED if no user is authenticated in
     *         the session, or 403 FORBIDDEN if the authenticated user is not the one who created this task
     */
    @PostMapping("/answer")
    public ResponseEntity<?> getAskStatus(@RequestBody AnswerRequest answerRequest, HttpSession session) {
        User user = accessGuard.currentUser(session);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> progress = taskProgress.get(answerRequest.taskId());
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        if (!Objects.equals(userKey(user, session), progress.get("userId"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String status = (String) progress.get("status");
        if (status.equals("done") || status.equals("error")) {
            taskProgress.remove(answerRequest.taskId());
            return ResponseEntity.ok(progress);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(progress);
    }

    /**
     * Gets the status of the documents being added to the given project.
     * The caller must be authenticated and have access to the given project.
     *
     * @param project the id of the project being added to
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return an {@link IndexStatusResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 401 UNAUTHORIZED if no user is authenticated in
     *         the session, 404 NOT_FOUND if the project does not exist, or 403 FORBIDDEN if the user does not have
     *         access to the project
     */
    @GetMapping("/status")
    public IndexStatusResponse getIndexStatus(@RequestParam long project, HttpSession session) {
        requireAccess(project, session);
        return ragService.allAdded(project);
    }

    /**
     * Uploads one or more files and begins asynchronously indexing them into the given project.
     * The caller must be authenticated and an admin of the given project.
     *
     * @param files the files to be indexed
     * @param projectId the id of the project the files should be added to
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return 200 OK with "Upload started" if the process was successfully initiated,
     *         or 500 Internal Server Error with an error message if an exception occurred
     * @throws org.springframework.web.server.ResponseStatusException 401 UNAUTHORIZED if no user is authenticated in
     *         the session, 404 NOT_FOUND if the project does not exist, or 403 FORBIDDEN if the user is not an admin
     *         of the project
     */
    @PostMapping("/add")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                              @RequestParam("project") long projectId, HttpSession session) {
        requireAdmin(projectId, session);
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
     * The caller must be authenticated and an admin of the given project.
     *
     * @param deleteProjectRequest a {@link DeleteProjectRequest} record
     * @param session the current HTTP session, expected to contain the authenticated user
     * @throws org.springframework.web.server.ResponseStatusException 401 UNAUTHORIZED if no user is authenticated in
     *         the session, 404 NOT_FOUND if the project does not exist, or 403 FORBIDDEN if the user is not an admin
     *         of the project
     */
    @PostMapping("/delete")
    public void deleteDocuments(@RequestBody DeleteProjectRequest deleteProjectRequest, HttpSession session) {
        requireAdmin(deleteProjectRequest.project(), session);
        ragService.deleteProject(deleteProjectRequest.project());
    }
}

