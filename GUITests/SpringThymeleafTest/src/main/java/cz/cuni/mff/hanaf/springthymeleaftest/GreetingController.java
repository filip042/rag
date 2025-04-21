package cz.cuni.mff.hanaf.springthymeleaftest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, @RequestParam(name="secret", required=false) String secret, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("secret", secret);
        return "greeting";
    }


}