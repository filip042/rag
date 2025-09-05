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

import java.util.ArrayList;
import java.util.List;
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

    @GetMapping("/form")
    public String loadForm() {
        return "load";
    }

    @GetMapping("/login")
    public String chooseUser(Model model) {
        List<User> users = userRepository.findAll();
        List<String> userNames = users.stream().map(User::getUsername).toList();
        model.addAttribute("user", new User());
        model.addAttribute("availableUsers", userNames);
        return "chooseUser";
    }

    private boolean passwordMatches(User user, String rawPassword) {
        return user.getPassword().equals(rawPassword);  // todo temp
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Project> projects = projectRepository.findByAccessibleUsers_Username(user.getUsername());
        List<String> projectNames = projects.stream().map(Project::getName).toList();
        model.addAttribute("project", new Project());
        model.addAttribute("availableProjects", projectNames);

        return "dashboard";
    }

    @PostMapping("/login")
    public String verifyUser(
            @ModelAttribute("user") User user,
            @RequestParam("password") String password,
            Model model,
            HttpSession session
    ) {
        User existingUser = userRepository.findByUsername(user.getUsername())
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
    public String myPage(@RequestParam(name = "question") String question, Model model) {
        String apiUrl = "http://localhost:8080/app/ask?query=" + question + "&workSpace=async1"; // todo temp
        String data = restTemplate.getForObject(apiUrl, String.class); // todo temp type

        data = Utils.removeThinking(data);

        System.out.println(data); // todo test remove

        model.addAttribute("data", data);
        return "answer";
    }

    @PostMapping("/load")
    public String loadDir(@RequestParam(name = "directory") String directory, @RequestParam(name = "workspace") String workspace) {
        String apiUrl = "http://localhost:8080/app/add?path=" + directory + "&workSpace=" + workspace; // todo temp
        restTemplate.getForObject(apiUrl, Void.class);
        // restTemplate.getForObject(apiUrl, Void.class);

        return "indexingResult"; // temp, show notification or something
    }
}

