package cz.cuni.mff.hanaf.mainapp.web;

import cz.cuni.mff.hanaf.mainapp.AppProperties;
import cz.cuni.mff.hanaf.mainapp.data.*;
import cz.cuni.mff.hanaf.mainapp.rag.QueryProperties;
import cz.cuni.mff.hanaf.mainapp.rag.dto.DeleteProjectRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;

/**
 * Thymeleaf controller handling page navigation, user authentication,
 * and project management for the main application frontend.
 */
@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final AppProperties appProperties;
    private final QueryProperties queryProperties;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new {@code ThymeLeafController} with the required dependencies.
     *
     * @param appProperties the application configuration providing endpoint URLs
     * @param queryProperties configuration properties for query handling
     * @param restTemplate the REST template used to forward requests to the API
     * @param userRepository repository for loading and persisting users
     * @param projectRepository repository for loading and persisting projects
     * @param questionRepository repository for loading question history
     * @param passwordEncoder the encoder used to hash and verify passwords
     */
    public ThymeLeafController(AppProperties appProperties, QueryProperties queryProperties, RestTemplate restTemplate, UserRepository userRepository, ProjectRepository projectRepository, QuestionRepository questionRepository, PasswordEncoder passwordEncoder) {
        this.appProperties = appProperties;
        this.queryProperties = queryProperties;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Displays the landing page.
     *
     * @return the landing page view name
     */
    @GetMapping("/")
    public String landingPage() {
        return "index";
    }

    /**
     * Displays the chat interface for the project stored in the session.
     *
     * @param session the current HTTP session, expected to contain the active project and authenticated user
     * @param model the model to add project and user data to
     * @return the chat view name, or a redirect to the dashboard if no project or user is in the session
     */
    @GetMapping("/chat")
    public String loadForm(HttpSession session, Model model) {
        Project currentProject = (Project) session.getAttribute("project");
        User currentUser = (User) session.getAttribute("authenticatedUser");
        if (currentProject == null || currentUser == null) {
            String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        }
        Project freshProject = projectRepository.findById(currentProject.getId()).orElse(currentProject); // so the warning banner is accurate
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("project", freshProject);
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("maxQueryLength", queryProperties.getMaxQueryLength());
        return "projectHome";
    }

    /**
     * Displays the login form.
     *
     * @param session the current HTTP session
     * @param model The model to add form data to
     * @return The form's view name
     */
    @GetMapping("/login")
    public String chooseUser(HttpSession session, Model model) {
        session.setAttribute("guest", false);
        model.addAttribute("user", new User());
        return "chooseUser";
    }

    private boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Displays the logged-in user's dashboard.
     *
     * @param session the HTTP session with the authenticated user
     * @param model the model to add project form data to
     * @return the dashboard view name, or a redirect to the logout page if no user is in the session
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        session.removeAttribute("project");
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            String logoutUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getLogout();
            return "redirect:" + logoutUrl;
        }

        Set<Project> projects = new HashSet<>(projectRepository.findByIsPublicTrue());
        if (user.isRegistered()) {
            projects.addAll(projectRepository.findByAccessibleUsers_Id(user.getId()));
            projects.addAll(projectRepository.findByAdminUsers_Id(user.getId()));
        }

        model.addAttribute("project", new Project());
        model.addAttribute("availableProjects", projects);
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("guest", session.getAttribute("guest"));

        return "dashboard";
    }

    /**
     * Sets the given project as the active project in the session and redirects to the chat page.
     *
     * @param projectId the id of the project to set in the session
     * @param session the current HTTP session
     * @return a redirect to the chat page or to the dashboard if no user is in the session
     */
    @PostMapping("/dashboard")
    public String verifyProject(@RequestParam("projectId") Long projectId, HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();

        if (user == null) {
            return "redirect:" + dashboardUrl;
        }

        projectRepository.findById(projectId).ifPresent(project -> {
            session.setAttribute("project", project);
            session.setAttribute("admin", user.isRegistered() && projectRepository.findByAdminUsers_Id(user.getId()).contains(project));
        });

        String chatUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getChat();

        return "redirect:" + chatUrl;
    }

    /**
     * Validates the submitted login credentials and logs the user in if correct.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     * @param model the model to add error and form data to
     * @param session the current HTTP session where the authenticated user is set on success
     * @return a redirect to the dashboard if credentials are valid, or the login view if validation fails
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
            String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        } else {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("attemptedUsername", username);
            return "chooseUser";
        }
    }

    /**
     * Logs the user in as a guest and redirect to to the dashboard.
     *
     * @param session the http session where the authenticated user is set
     * @return a redirect to the dashboard
     */
    @PostMapping("/guest")
    public String guestLogin(HttpSession session) {
        User guestUser = new User();
        guestUser.setUsername("Guest");
        session.setAttribute("authenticatedUser", guestUser);
        session.setAttribute("guest", true);
        String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
        return "redirect:" + dashboardUrl;
    }

    /**
     * Logs out the current user by invalidating the HTTP session.
     *
     * @param session the HTTP session with the current user
     * @return a redirect to the login page
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        String loginUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getLogin();
        return "redirect:" + loginUrl;
    }

    /**
     * Prepares the answer page for the given question using the project stored in the session.
     *
     * @param question the question to be answered by the LLM
     * @param model the model to add the question, project, and API endpoint URLs to
     * @param session the current HTTP session, expected to contain the active project
     * @return the answer view name, or a redirect to the dashboard if no project is set in the session
     */
    @PostMapping("/answer")
    public String myPage(@RequestParam(name = "question") String question, Model model, HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        }
        String askUrl = appProperties.getBaseUrl() + appProperties.getApiUrls().getBase() + appProperties.getApiUrls().getAsk();
        String answerUrl = appProperties.getBaseUrl() + appProperties.getApiUrls().getBase() + appProperties.getApiUrls().getAnswer();
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("question", question);
        model.addAttribute("project", project.getId());
        model.addAttribute("askUrl", askUrl);
        model.addAttribute("answerUrl", answerUrl);
        model.addAttribute("currentUsername", currentUser.getUsername());

        return "answer";
    }

    /**
     * Displays the user creation form.
     *
     * @param model the model to which the user is added
     * @return the view name for the form
     */
    @GetMapping("/newUser")
    public String showNewUserForm(Model model) {
        model.addAttribute("user", new User());
        return "newUser";
    }

    /**
     * Validates and creates a new user from the submitted form data.
     * On success, stores the new user in the session and redirects to the dashboard.
     *
     * @param user the user to create, populated from the form
     * @param confirmPassword the confirmation password entered in the form
     * @param model the model to add error attributes to if validation fails
     * @param session the current HTTP session
     * @return a redirect to the dashboard if the user is successfully created, or the new user form view if validation fails or an error occurs
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
            String dashboardUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
            return "redirect:" + dashboardUrl;
        } catch (Exception e) { // todo
            model.addAttribute("error", "Error creating user");
            return "newUser";
        }
    }

    /**
     * Displays the form for creating projects
     *
     * @param model The model to which the project is added
     * @param session the current HTTP session, expected to contain the authenticated user
     * @return The view name for the form
     */
    @GetMapping("/newProject")
    public String showNewProjectForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("project", new Project());
        model.addAttribute("currentUsername", currentUser.getUsername());
        return "newProject";
    }

    /**
     * Creates and validates a new project associated with the current user from the data entered in the form.
     *
     * @param project the project to create, populated from the form
     * @param session the current HTTP session, expected to contain the authenticated user
     * @param model the model to add error attributes to if validation fails
     * @return a redirect to the chat page if the project is successfully created, or the new project form view if validation fails or an error occurs
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
            String url = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getChat();
            return "redirect:" + url;
        } catch (Exception e) { // todo
            model.addAttribute("error", "Error creating project");
            return "newProject";
        }
    }

    /**
     * Deletes the logged-in user and all projects which they are the only admin of.
     *
     * @param session the current HTTP session, expected to contain the logged-in user
     * @return a redirect string to the logout page
     */
    @PostMapping("/deleteUser")
    public String deleteUser(HttpSession session) {
        User user = (User) session.getAttribute("authenticatedUser");
        String url = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getLogout();
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
     * Deletes the project open in the current session.
     *
     * @param session the current http session, expected to contain the current project
     * @return a redirect string to the dashboard view
     */
    @PostMapping("/deleteProject")
    public String deleteProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        String url = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
        if (project == null) {
            return "redirect:" + url;
        }
        questionRepository.deleteByProjectId(project.getId());
        projectRepository.deleteById(project.getId());
        String apiUrl = appProperties.getBaseUrl() + appProperties.getApiUrls().getBase() + appProperties.getApiUrls().getDelete();
        DeleteProjectRequest request = new DeleteProjectRequest(project.getId());

        restTemplate.postForObject(apiUrl, request, Void.class);
        return "redirect:" + url;
    }

    /**
     * Removes the authenticated user from the project stored in the session.
     *
     * @param session the current HTTP session, expected to contain the active project and authenticated user
     * @return a redirect to the dashboard
     */
    @PostMapping("/exitProject")
    public String exitProject(HttpSession session) {
        Project project = (Project) session.getAttribute("project");
        User user = (User) session.getAttribute("authenticatedUser");
        String url = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getDashboard();
        if (project == null || user == null) {
            return "redirect:" + url;
        }
        Project realProject = projectRepository.findByIdWithUsers(project.getId())
                .orElseThrow(() -> new IllegalStateException("Project not found"));
        realProject.removeAccessibleUser(user);
        return "redirect:" + url;
    }

    /**
     * Displays the admin settings page for the current project.
     *
     * @param session the current HTTP session, expected to contain the current project and authenticated user
     * @param model the model to add project, user, and endpoint data to
     * @return the admin settings view name, or a redirect to the logout page if no project is in the session
     */
    @PostMapping("/admin")
    public String adminSettings(HttpSession session, Model model) {
        Project sessionProject = (Project) session.getAttribute("project");
        if (sessionProject == null) {
            String logoutUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getLogout();
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

        Set<User> unadded;
        unadded = new HashSet<>(userRepository.findAll());
        unadded.removeAll(project.getAdminUsers());
        if (!project.isPublic()) {
            unadded.removeAll(project.getAccessibleUsers());
        }

        model.addAttribute("user", new User());
        model.addAttribute("unadded", unadded);
        model.addAttribute("users", usersList);

        String url = appProperties.getBaseUrl() + appProperties.getApiUrls().getBase() + appProperties.getApiUrls().getStatus();
        String loadUrl = appProperties.getBaseUrl() + "/admin" + appProperties.getFrontendUrls().getLoad(); // todo temp
        String parameter = "?project=" + sessionProject.getId();
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("articleCountEndpoint", url.concat(parameter));
        model.addAttribute("loadEndpoint", loadUrl);
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("project", project);

        return "adminSettings";
    }

    /**
     * Displays the question history for the current project.
     *
     * @param session the current HTTP session, expected to contain the current project
     * @param model the model to add the question list and user data to
     * @return the history view name, or a redirect to the logout page if no project is in the session
     */
    @GetMapping("/history")
    public String showHistory(HttpSession session, Model model) {
        Project project = (Project) session.getAttribute("project");
        if (project == null) {
            String logoutUrl = appProperties.getFrontendUrls().getBase() + appProperties.getFrontendUrls().getLogout();
            return "redirect:" + logoutUrl;
        }

        List<Question> questions = questionRepository.findByProject_Id(project.getId());
        User currentUser = (User) session.getAttribute("authenticatedUser");
        model.addAttribute("questions", questions);
        model.addAttribute("currentUsername", currentUser.getUsername());

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
