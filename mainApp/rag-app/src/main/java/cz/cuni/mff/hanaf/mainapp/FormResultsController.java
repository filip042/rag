package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.data.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    // todo
    @PostMapping("/load")
    public String loadFiles(@RequestParam("files") MultipartFile[] files, HttpSession session) {
        String apiUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAdd();
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

    @PostMapping("/visibility")
    public Map<String, Object> setVisibility(@RequestParam boolean isPublic, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setPublic(isPublic);
        projectRepository.save(project);

        return Map.of("isPublic", isPublic);
    }

    @PostMapping("/archive")
    public Map<String, Object> setArchived(@RequestParam boolean isArchived, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findById(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        project.setArchived(isArchived);
        projectRepository.save(project);
        return Map.of("isArchived", isArchived);
    }

    @PostMapping("/add")
    public Map<String, Object> addUser(@RequestParam Long userId, HttpSession session) {
        Project sessionProject = (Project) session.getAttribute("project");
        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow();
        if (project.isPublic()) {
            project.getAccessibleUsers().remove(user);
            project.getAdminUsers().add(user);
        } else {
            project.getAccessibleUsers().add(user);
        }
        projectRepository.save(project);

        return Map.of("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "admin", project.isPublic()
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
