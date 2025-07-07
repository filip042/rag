package cz.cuni.mff.hanaf.mainapp;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final RestTemplate restTemplate;

    public ThymeLeafController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/form")
    public String loadForm() {
        return "form";
    }

    @PostMapping("/answer")
    public String myPage(@RequestParam(name = "question", required = true) String question, Model model) {
        String apiUrl = "http://localhost:8080/app/ask?query=" + question + "&workSpace=async1"; // todo temp
        String data = restTemplate.getForObject(apiUrl, String.class); // todo temp type

        data = Utils.removeThinking(data);

        System.out.println(data); // todo test remove

        model.addAttribute("data", data);
        return "answer";
    }
}