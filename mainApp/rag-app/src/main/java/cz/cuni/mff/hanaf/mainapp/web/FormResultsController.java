package cz.cuni.mff.hanaf.mainapp.web;

import cz.cuni.mff.hanaf.mainapp.AppProperties;
import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.data.UserRepository;
import cz.cuni.mff.hanaf.mainapp.dto.ArchiveResponse;
import cz.cuni.mff.hanaf.mainapp.dto.UserResponse;
import cz.cuni.mff.hanaf.mainapp.dto.VisibilityResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller handling various form submissions.
 */
@RestController
@RequestMapping("/admin")
public class FormResultsController {
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * Creates a new {@code FormResultsController} with the required dependencies.
     *
     * @param appProperties the application configuration providing endpoint URLs
     * @param restTemplate the REST template used to forward file uploads to the API
     * @param userRepository repository for loading users
     * @param projectRepository repository for loading and persisting projects
     */
    public FormResultsController(AppProperties appProperties, RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Forwards the uploaded files to the indexing API for the project stored in the session.
     *
     * @param files the files to upload
     * @param session the current HTTP session, expected to contain the active project
     * @return "OK" if a project is set in the session, or "NO_PROJECT" otherwise
     */
    @PostMapping("/load")
    public String loadFiles(@RequestParam("files") MultipartFile[] files, HttpSession session) {
        String apiUrl = appProperties.getBaseUrl() + appProperties.getApiUrls().getBase() + appProperties.getApiUrls().getAdd();
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            return "NO_PROJECT";
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add("files", file.getResource());
        }
        body.add("project", project.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        restTemplate.postForObject(apiUrl, new HttpEntity<>(body, headers), String.class);

        return "OK";
    }

    /**
     * Sets the visibility of the project stored in the session.
     *
     * @param isPublic {@code true} to make the project public, and {@code false} to make it private
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link VisibilityResponse} record
     */
    @PostMapping("/visibility")
    public VisibilityResponse setVisibility(@RequestParam boolean isPublic, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setPublic(isPublic);
        projectRepository.save(project);

        return new VisibilityResponse(isPublic);
    }

    /**
     * Sets the archivation of the project stored in the session.
     *
     * @param isArchived {@code true} to archive the project, and {@code false} to unarchive it
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link ArchiveResponse} record
     */
    @PostMapping("/archive")
    public ArchiveResponse setArchived(@RequestParam boolean isArchived, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setArchived(isArchived);
        projectRepository.save(project);
        return new ArchiveResponse(isArchived);
    }

    /**
     * Adds the given user to the project stored in the session.
     * For public projects, the default is an admin, and for private projects, a regular user.
     *
     * @param userId the id of the user to add
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     */
    @PostMapping("/add")
    public UserResponse addUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
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
     *
     * @param userId the id of the user to promote
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     */
    @PostMapping("/promote")
    public UserResponse promoteUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.addAdminUser(user);
        project.removeAccessibleUser(user);
        projectRepository.save(project);

        return new UserResponse(user.getId(), user.getUsername(), true);
    }

    /**
     * Adds the given user from the project stored in the session.
     *
     * @param userId the id of the user to remove
     * @param session the current HTTP session, expected to contain the active project
     * @return a {@link UserResponse} record
     */
    @PostMapping("/remove")
    public UserResponse removeUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.removeAdminUser(user);
        project.removeAccessibleUser(user);
        projectRepository.save(project);

        return new UserResponse(user.getId(), user.getUsername(), false);
    }
}
