package cz.cuni.mff.hanaf.mainapp;


import cz.cuni.mff.hanaf.mainapp.data.Project;
import cz.cuni.mff.hanaf.mainapp.data.ProjectRepository;
import cz.cuni.mff.hanaf.mainapp.data.User;
import cz.cuni.mff.hanaf.mainapp.data.UserRepository;
import cz.cuni.mff.hanaf.mainapp.rag.Utils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ThymeLeafController(RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/chat")
    public String loadForm(@RequestParam(value = "projectId", required = false) Long projectId, HttpSession session) {
        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(project -> session.setAttribute("project", project));
        }
        return "load";
    }


    @GetMapping("/login")
    public String chooseUser(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("user", new User());
        model.addAttribute("availableUsers", users);
        return "chooseUser";
    }

    private boolean passwordMatches(User user, String rawPassword) {
        return user.getPassword().equals(rawPassword);  // todo temp
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        session.removeAttribute("project");
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Project> projects = projectRepository.findByAccessibleUsers_Username(user.getUsername());
        model.addAttribute("project", new Project());
        model.addAttribute("availableProjects", projects);

        return "dashboard";
    }

    @PostMapping("/login")
    public String verifyUser(
            @ModelAttribute("userId") Long id,
            @RequestParam("password") String password,
            Model model,
            HttpSession session
    ) {
        User existingUser = userRepository.findById(id)
                .orElse(null);

        if (existingUser != null && passwordMatches(existingUser, password)) {
            session.setAttribute("authenticatedUser", existingUser);
            return "redirect:/user/dashboard";
        } else {
            model.addAttribute("error", "Invalid username or password");

            List<String> userNames = userRepository.findAll().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());

            model.addAttribute("availableUsers", userNames);
            return "chooseUser";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("authenticatedUser");
        return "redirect:/user/login";
    }


    @PostMapping("/answer")
    public String myPage(@RequestParam(name = "question") String question, Model model, HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        String apiUrl = "http://localhost:8080/app/ask?query=" + question + "&workSpace=" + project.getId(); // todo temp
        String data = restTemplate.getForObject(apiUrl, String.class); // todo temp type

        data = Utils.removeThinking(data);

        System.out.println(data); // todo test remove

        String[] lines = data.strip().split("\\R");
        Set<String> sources = new HashSet<>();

        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1].trim();
            if (!lastLine.isEmpty()) {
                Pattern p = Pattern.compile("[^,]+?\\.[A-Za-z0-9]+"); // e.g., "file-name.md"
                Matcher m = p.matcher(lastLine);
                while (m.find()) {
                    sources.add(m.group().trim());
                }
            }
            data = Arrays.stream(lines)
                    .limit(lines.length - 1)
                    .collect(Collectors.joining("\n"));
        }

        System.out.println("sources: " + sources); // todo cross-reference with actual files
        System.out.println(data);

        model.addAttribute("data", data);
        model.addAttribute("sources", sources);
        return "answer";
    }

    @PostMapping("/load")
    public String loadDir(@RequestParam(name = "directory") String directory, HttpSession session) {
        String apiUrl = "http://localhost:8080/app/add";
        Map<String, Object> params = new HashMap<>();
        Project project = (Project) session.getAttribute("project");
        params.put("path", directory);
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);

        return "indexingResult"; // temp, show notification or something
    }

    @GetMapping("/newUser")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        return "newUser";
    }

    @PostMapping("/newUser")
    public String createNewUser(
            @ModelAttribute("user") User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty");
            return "newUser";
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty");
            return "newUser";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "newUser";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "newUser";
        }
        try {
            userRepository.save(user);
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user");
            return "newUser";
        }
    }

    @GetMapping("/newProject")
    public String showNewProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "newProject";
    }

    @PostMapping("/newProject")
    public String createNewProject(
            @ModelAttribute("project") Project project,
            HttpSession session,
            Model model
    ) {
        project.addAccessibleUser((User)session.getAttribute("authenticatedUser"));
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            model.addAttribute("error", "Name cannot be empty");
            return "newProject";
        }
        try {
            projectRepository.save(project);
            return "redirect:/user/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user");
            return "newProject";
        }
    }

    @PostMapping("/deleteProject")
    public String deleteProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        projectRepository.deleteById(project.getId());
        String apiUrl = "http://localhost:8080/app/delete";
        Map<String, Long> params = new HashMap<>();
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);
        return "redirect:/user/dashboard";
    }
}

