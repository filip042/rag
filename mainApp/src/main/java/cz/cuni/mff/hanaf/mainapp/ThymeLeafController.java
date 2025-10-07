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
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final AppConfig appConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public ThymeLeafController(AppConfig appConfig, RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository) {
        this.appConfig = appConfig;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Loads the form for working with the LLM
     *
     * @param projectId The ID of the current project
     * @param session The http session with the current project, if such a project exists
     * @return The template name of the form
     */
    @PostMapping("/chat")
    public String loadForm(@RequestParam(value = "projectId", required = false) Long projectId, HttpSession session, Model model) {
        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(project -> session.setAttribute("project", project));
        }
        String url = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getStatus();
        String parameter = "?workSpace=" + ((Project)session.getAttribute("project")).getId();
        model.addAttribute("articleCountEndpoint", url.concat(parameter));
        return "load";  // todo redirect if projectId doesn't exist
    }

    /**
     * Displays the form for logging in
     *
     * @param model The model to which the data for the form is added
     * @return The form's view name
     */
    @GetMapping("/login")
    public String chooseUser(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("user", new User());
        model.addAttribute("availableUsers", users);
        return "chooseUser";
    }

    /**
     * Checks if the entered password matches the given user's password
     *
     * @param user The user being logged into
     * @param rawPassword The entered password
     * @return True if the user's password matches the entered password, false otherwise
     */
    private boolean passwordMatches(User user, String rawPassword) {
        return user.getPassword().equals(rawPassword);  // todo temp
    }

    /**
     * Displays the logged-in user's dashboard
     *
     * @param session The http session with the authenticated user
     * @param model The model to add project form data to
     * @return a view name or redirect string:
     *         - "redirect:/user/login" if the authenticated user is not set
     *         - "dashboard" if the authenticated user is set
     */
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

    /**
     * Checks if the login credentials are valid, and if they are, logs the user in
     *
     * @param id The id of the user chosen
     * @param password The password entered by the user
     * @param model The model to add errors and form data to
     * @param session The http session where the authenticated user is set
     * @return a view name or redirect string:
     *         - "redirect:/user/dashboard" if the user's login credentials are correct
     *         - "chooseUser" if validation fails
     */
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

            List<User> users = userRepository.findAll();

            model.addAttribute("availableUsers", users);
            return "chooseUser";
        }
    }

    /**
     * Logs the current user out
     *
     * @param session The http session with the current user
     * @return A redirect string to the login page
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("authenticatedUser");
        return "redirect:/user/login";
    }

    /**
     * Gets the answer to the question from the LLM along with sources
     *
     * @param question The question to be answered by the LLM
     * @param model The model to add the answer and sources to
     * @param session The current http session with the current project // todo standardize workspace/project naming
     * @return The answer display view
     */
    @PostMapping("/answer")
    public String myPage(@RequestParam(name = "question") String question, Model model, HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        String askUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAsk();
        String answerUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAnswer();
        model.addAttribute("question", question);
        model.addAttribute("workSpace", project.getId());
        model.addAttribute("askUrl", askUrl);
        model.addAttribute("answerUrl", answerUrl);

        return "answer";
    }

    /**
     * Adds the documents in the directory to the workspace
     *
     * @param directory The directory with the documents to be added
     * @param session The current http session with the current workspace
     * @return The view name for the results page
     */
    @PostMapping("/load")
    public String loadDir(@RequestParam(name = "directory") String directory, HttpSession session) {
        String apiUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAdd();
        Map<String, Object> params = new HashMap<>();
        Project project = (Project) session.getAttribute("project");
        params.put("path", directory);
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);

        return "indexingResult"; // todo temp, show notification or something
    }

    /**
     * Displays the form for creating users
     *
     * @param model The model to which the user is added
     * @return The view name for the form
     */
    @GetMapping("/newUser")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        return "newUser";
    }

    /**
     * Creates and validates a new user from the data entered in the form
     *
     * @param user The user to be created, populated with data from the form
     * @param confirmPassword The confirmation password entered in the form
     * @param model The model to add error attributes to if validation fails
     * @return a view name or redirect string:
     *         - "redirect:/user/login" if the user is successfully created
     *         - "newUser" if validation fails or an error occurs during project creation
     */
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

    /**
     * Displays the form for creating projects
     *
     * @param model The model to which the project is added
     * @return The view name for the form
     */
    @GetMapping("/newProject")
    public String showNewProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "newProject";
    }

    /**
     * Creates and validates a new project associated with the current user from the data entered in the form
     *
     * @param project The Project object to be created, populated from the form
     * @param session The current http session with the current authenticated user
     * @param model The model to add error attributes to if validation fails
     * @return a view name or redirect string:
     *         - "redirect:/user/dashboard" if the project is successfully created
     *         - "newProject" if validation fails or an error occurs during project creation
     */
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
            String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
            return "redirect:" + url;
        } catch (Exception e) {
            model.addAttribute("error", "Error creating user");
            return "newProject";
        }
    }

    /**
     * Deletes the project open in the current session
     *
     * @param session The current http session
     * @return a redirect string to the user's dashboard page
     */
    @PostMapping("/deleteProject")
    public String deleteProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        projectRepository.deleteById(project.getId());
        String apiUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getDelete();
        Map<String, Long> params = new HashMap<>();
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);
        String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
        return "redirect:" + url;
    }
}
