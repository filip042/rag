package cz.cuni.mff.hanaf.mainapp;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final RestTemplate restTemplate;

    public ThymeLeafController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/form")
    public String loadForm() {
        return "load";
    }

    @GetMapping("/user")
    public String chooseUser(Model model) {
        List<String> userNames = new ArrayList<>(); // todo database
        userNames.add("John");
        userNames.add("Jane");
        userNames.add("Joe");
        model.addAttribute("user", new User());
        model.addAttribute("availableUsers", userNames);
        return "chooseUser";
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

