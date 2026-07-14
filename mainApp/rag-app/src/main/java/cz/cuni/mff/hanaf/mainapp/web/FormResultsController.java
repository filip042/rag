package cz.cuni.mff.hanaf.mainapp.web;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.data.UserRepository;
import cz.cuni.mff.hanaf.mainapp.rag.RagService;
import cz.cuni.mff.hanaf.mainapp.security.AccessGuard;
import cz.cuni.mff.hanaf.mainapp.web.dto.ArchiveResponse;
import cz.cuni.mff.hanaf.mainapp.web.dto.UserResponse;
import cz.cuni.mff.hanaf.mainapp.web.dto.VisibilityResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller handling various form submissions.
 */
@RestController
@RequestMapping("/admin")
public class FormResultsController {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AccessGuard accessGuard;
    private final RagService ragService;

    /**
     * Creates a new {@code FormResultsController} with the required dependencies.
     *
     * @param userRepository repository for loading users
     * @param projectRepository repository for loading and persisting projects
     * @param accessGuard the access guard for verifying permission for accesing specific projects
     * @param ragService the service used for RAG-related operations, such as document indexing
     */
    public FormResultsController(UserRepository userRepository, ProjectRepository projectRepository, AccessGuard accessGuard, RagService ragService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.accessGuard = accessGuard;
        this.ragService = ragService;
    }

    /**
     * Forwards the uploaded files to the indexing API for the project stored in the session.
     * The caller must be an admin of the project stored in the session.
     *
     * @param files the files to upload
     * @param session the current HTTP session, expected to contain the active project
     * @return "OK" if the upload was forwarded successfully
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/load")
    public String loadFiles(@RequestParam("files") MultipartFile[] files, HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        if (project == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        ragService.addDocuments(files, project.getId());

        return "OK";
    }

    /**
     * Sets the visibility of the project stored in the session.
     * The caller must be an admin of the project stored in the session.
     *
     * @param isPublic {@code true} to make the project public, and {@code false} to make it private
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link VisibilityResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/visibility")
    public VisibilityResponse setVisibility(@RequestParam boolean isPublic, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setPublic(isPublic);
        projectRepository.save(project);

        return new VisibilityResponse(isPublic);
    }

    /**
     * Sets the archivation of the project stored in the session.
     * The caller must be an admin of the project stored in the session.
     *
     * @param isArchived {@code true} to archive the project, and {@code false} to unarchive it
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link ArchiveResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/archive")
    public ArchiveResponse setArchived(@RequestParam boolean isArchived, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setArchived(isArchived);
        projectRepository.save(project);
        return new ArchiveResponse(isArchived);
    }

    /**
     * Adds the given user to the project stored in the session.
     * For public projects, the default is an admin, and for private projects, a regular user.
     * The caller must be an admin of the project stored in the session.
     *
     * @param userId the id of the user to add
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/add")
    public UserResponse addUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        if (project.isPublic()) {
            project.removeAccessibleUser(user);
            project.addAdminUser(user);
        } else {
            project.addAccessibleUser(user);
        }
        projectRepository.save(project);

        return new UserResponse(user.getId(), user.getUsername(), project.isPublic());
    }

    /**
     * Promotes the given user to admin in the project stored in the session.
     * The caller must be an admin of the project stored in the session.
     *
     * @param userId the id of the user to promote
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/promote")
    public UserResponse promoteUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.addAdminUser(user);
        project.removeAccessibleUser(user);
        projectRepository.save(project);

        return new UserResponse(user.getId(), user.getUsername(), true);
    }

    /**
     * Removes the given user from the project stored in the session.
     * The caller must be an admin of the project stored in the session.
     *
     * @param userId the id of the user to remove
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     * @throws org.springframework.web.server.ResponseStatusException 403 FORBIDDEN if no project is set in the session
     *         or if the caller is not an admin of the session project
     */
    @PostMapping("/remove")
    public UserResponse removeUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null || !accessGuard.isAdminOfSessionProject(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.removeAdminUser(user);
        project.removeAccessibleUser(user);
        projectRepository.save(project);

        return new UserResponse(user.getId(), user.getUsername(), false);
    }
}
