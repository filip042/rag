package cz.cuni.mff.hanaf.mainapp.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for handling application errors.
 */
@Controller
public class WebErrorController implements ErrorController {

    /**
     * Processes the error request and adds the HTTP status code to the model.
     *
     * @param request the HTTP request containing error attributes
     * @param model the model for the view
     * @return the error view name
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("statusCode", statusCode);
        }
        return "error";
    }
}
