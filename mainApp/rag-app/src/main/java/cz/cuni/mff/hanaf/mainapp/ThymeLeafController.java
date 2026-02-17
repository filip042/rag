package cz.cuni.mff.hanaf.mainapp;

import cz.cuni.mff.hanaf.mainapp.data.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final AppConfig appConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    public ThymeLeafController(AppConfig appConfig, RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository, QuestionRepository questionRepository, PasswordEncoder passwordEncoder) {
        this.appConfig = appConfig;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String landingPage() {
        return "index";
    }

    /**
     * Loads the form for working with the LLM
     *
     * @param session The http session with the current project, if such a project exists
     * @return a view name or redirect string:
     *         - The template name of the form if the project id is valid
     *         - "redirect:/user/logout" otherwise
     */
    @GetMapping("/chat")
    public String loadForm(HttpSession session, Model model) {
        Project currentProject = (Project) session.getAttribute("project");
        User currentUser = (User) session.getAttribute("authenticatedUser");
        if (currentProject == null || currentUser == null) {
            String dashboardUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        }
        model.addAttribute("currentUser", currentUser.getUsername());
        model.addAttribute("project", currentProject);
        model.addAttribute("admin", session.getAttribute("admin"));
        return "load";
    }

    /**
     * Displays the form for logging in
     *
     * @param model The model to which the data for the form is added
     * @return The form's view name
     */
    @GetMapping("/login")
    public String chooseUser(Model model) {
        model.addAttribute("user", new User());
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
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Displays the logged-in user's dashboard
     *
     * @param session The http session with the authenticated user
     * @param model The model to add project form data to
     * @return a view name or redirect string:
     *         - "redirect:/user/logout" if the authenticated user is not set
     *         - "dashboard" if the authenticated user is set
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        session.removeAttribute("project");
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            String logoutUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getLogout();
            return "redirect:" + logoutUrl;
        }

        List<Project> projects = projectRepository.findByAccessibleUsers_Id(user.getId());
        List<Project> adminProjects = projectRepository.findByAdminUsers_Id(user.getId());
        projects.addAll(adminProjects);
        model.addAttribute("project", new Project());
        model.addAttribute("availableProjects", projects);
        model.addAttribute("currentUser", user.getUsername());

        return "dashboard";
    }

    @PostMapping("/dashboard")
    public String verifyProject(@RequestParam("projectId") Long projectId, HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        String dashboardUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();

        if (user == null) {
            return "redirect:" + dashboardUrl;
        }

        projectRepository.findById(projectId).ifPresent(project -> {
            session.setAttribute("project", project);
            session.setAttribute("admin", projectRepository.findByAdminUsers_Id(user.getId()).contains(project));
        });

        String chatUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getChat();

        return "redirect:" + chatUrl;
    }

    /**
     * Checks if the login credentials are valid, and if they are, logs the user in
     *
     * @param username The username entered by the user
     * @param password The password entered by the user
     * @param model The model to add errors and form data to
     * @param session The http session where the authenticated user is set
     * @return a view name or redirect string:
     *         - "redirect:/user/dashboard" if the user's login credentials are correct
     *         - "chooseUser" if validation fails
     */
    @PostMapping("/login")
    public String verifyUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session
    ) {
        User existingUser = userRepository.findByUsername(username)
                .orElse(null);

        if (existingUser != null && passwordMatches(existingUser, password)) {
            session.setAttribute("authenticatedUser", existingUser);
            String dashboardUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        } else {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("attemptedUsername", username);
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
        session.invalidate();
        String loginUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getLogin();
        return "redirect:" + loginUrl;
    }

    /**
     * Gets the answer to the question from the LLM along with sources
     *
     * @param question The question to be answered by the LLM
     * @param model The model to add the answer and sources to
     * @param session The current http session with the current project // todo standardize workspace/project naming
     * @return a view name or redirect string:
     *         - The answer display view if the session has a valid project
     *         - "redirect:/user/dashboard" otherwise
     */
    @PostMapping("/answer")
    public String myPage(@RequestParam(name = "question") String question, Model model, HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            String dashboardUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        }
        String askUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAsk();
        String answerUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getAnswer();
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("question", question);
        model.addAttribute("workSpace", project.getId());
        model.addAttribute("askUrl", askUrl);
        model.addAttribute("answerUrl", answerUrl);
        model.addAttribute("currentUser", currentUser.getUsername());

        return "answer";
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
            Model model,
            HttpSession session
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
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            session.setAttribute("authenticatedUser", savedUser);
            String dashboardUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
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
        project.addAdminUser((User)session.getAttribute("authenticatedUser"));
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            model.addAttribute("error", "Name cannot be empty");
            return "newProject";
        }
        try {
            Project savedProject = projectRepository.save(project);
            session.setAttribute("project", savedProject);
            session.setAttribute("admin", true);
            String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getChat();
            return "redirect:" + url;
        } catch (Exception e) {
            model.addAttribute("error", "Error creating project");
            return "newProject";
        }
    }

    /**
     * Deletes the logged-in user
     *
     * @param session The current http session
     * @return a redirect string to the logout page
     */
    @PostMapping("/deleteUser")
    public String deleteUser(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getLogout();
        if (user == null) {
            return "redirect:" + url;
        }

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Project> adminProjects = projectRepository.findByAdminUsers_Id(managedUser.getId());
        List<Long> projectIdsToDelete = adminProjects.stream()
                .filter(project -> project.getAdminUsers().size() == 1)
                .map(Project::getId)
                .toList();

        projectIdsToDelete.forEach(projectRepository::deleteById);

        projectRepository.findByAccessibleUsers_Id(managedUser.getId()).stream()
                .filter(project -> !projectIdsToDelete.contains(project.getId()))
                .forEach(project -> {
                    project.removeAccessibleUser(managedUser);
                    projectRepository.save(project);
                });

        adminProjects.stream()
                .filter(project -> !projectIdsToDelete.contains(project.getId()))
                .forEach(project -> {
                    project.removeAdminUser(managedUser);
                    projectRepository.save(project);
                });

        userRepository.deleteById(managedUser.getId());

        return "redirect:" + url;
    }

    /**
     * Deletes the project open in the current session
     *
     * @param session The current http session
     * @return a redirect string to the dashboard view
     */
    @PostMapping("/deleteProject")
    public String deleteProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
        if (project == null) {
            return "redirect:" + url;
        }
        projectRepository.deleteById(project.getId());
        String apiUrl = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getDelete();
        Map<String, Long> params = new HashMap<>();
        params.put("workSpace", project.getId());

        restTemplate.postForObject(apiUrl, params, Void.class);
        return "redirect:" + url;
    }

    @PostMapping("/exitProject")
    public String exitProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        User user = (User) session.getAttribute("authenticatedUser");
        String url = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getDashboard();
        if (project == null || user == null) {
            return "redirect:" + url;
        }
        Project realProject = projectRepository.findByIdWithUsers(project.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        realProject.removeAccessibleUser(user);
        return "redirect:" + url;
    }

    @PostMapping("/admin")
    public String adminSettings(HttpSession session, Model model) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null) {
            String logoutUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getLogout();
            return "redirect:" + logoutUrl;
        }

        Project project = projectRepository.findByIdWithUsers(sessionProject.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));

        Set<User> admins = project.getAdminUsers();
        Set<User> accessible = project.getAccessibleUsers();

        List<Map<String, Object>> usersList = Stream.concat(
                mapUsers(admins, true).stream(),
                mapUsers(accessible, false).stream()
        ).toList();

        Set<User> projectUsers = new HashSet<>();
        projectUsers.addAll(admins);
        projectUsers.addAll(accessible);

        List<User> usersNotInProject = userRepository.findAll().stream()
                .filter(u -> !projectUsers.contains(u))
                .toList();

        model.addAttribute("user", new User());
        model.addAttribute("unadded", usersNotInProject);
        model.addAttribute("users", usersList);

        String url = appConfig.getBaseUrl() + appConfig.getApiUrls().getBase() + appConfig.getApiUrls().getStatus();
        String loadUrl = appConfig.getBaseUrl() + "/admin" + appConfig.getFrontendUrls().getLoad(); // todo temp
        String parameter = "?workSpace=" + sessionProject.getId();
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("articleCountEndpoint", url.concat(parameter));
        model.addAttribute("loadEndpoint", loadUrl);
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("currentUser", currentUser.getUsername());

        return "adminSettings";
    }

    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            String logoutUrl = appConfig.getFrontendUrls().getBase() + appConfig.getFrontendUrls().getLogout();
            return "redirect:" + logoutUrl;
        }

        List<Question> questions = questionRepository.findByProject_Id(project.getId());
        model.addAttribute("questions", questions);

        return "history";
    }

    private List<Map<String, Object>> mapUsers(Set<User> users, boolean isAdmin) {
        return users.stream().map(u -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("username", u.getUsername());
            row.put("admin", isAdmin);
            return row;
        }).toList();
    }
}
