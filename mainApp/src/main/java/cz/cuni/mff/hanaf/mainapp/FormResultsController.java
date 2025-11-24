package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.data.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class FormResultsController {
    private final AppConfig appConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public FormResultsController(AppConfig appConfig, RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository) {
        this.appConfig = appConfig;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Adds the documents in the directory to the workspace
     *
     * @param directory The directory with the documents to be added
     * @param session The current http session with the current workspace
     * @return a view name or redirect string:
     *         - The view name for the results page if the session has a valid project
     *         - "redirect:/user/dashboard" otherwise
     */
    @PostMapping("/load")
    public String loadDir(@RequestParam(name = "directory") String directory, HttpSession session) {
        String apiUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAdd();
        Map<String, Object> params = new HashMap<>();
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            return "NO_PROJECT";
        }
        params.put("path", directory);
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);

        return "OK";
    }

    @PostMapping("/add")
    public Map<String, Object> addUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.getAccessibleUsers().add(user);
        projectRepository.save(project);

        return Map.of("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "admin", false
        ));
    }

    @PostMapping("/promote")
    public Map<String, Object> promoteUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.getAdminUsers().add(user);
        project.getAccessibleUsers().remove(user);
        projectRepository.save(project);

        return Map.of("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "admin", true
        ));
    }

    @PostMapping("/remove")
    public Map<String, Object> removeUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        project.getAdminUsers().remove(user);
        project.getAccessibleUsers().remove(user);
        projectRepository.save(project);

        return Map.of("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        ));
    }
}
