package cz.cuni.mff.hanaf.mainapp;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/user")
public class ThymeLeafController {

    private final RestTemplate restTemplate;

    public ThymeLeafController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/main")
    public String myPage(@RequestParam(name = "id", required = false) Long id, Model model) {
        String apiUrl = "http://localhost:8080/app/ask?query=what-fruit-is-the-sky?&workSpace=async1"; // todo temp
        String data = restTemplate.getForObject(apiUrl, String.class); // todo temp type

        model.addAttribute("data", data);
        return "main";
    }
}