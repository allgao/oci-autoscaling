package ociautoscaling.controller;

import ociautoscaling.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    LoginService service;

    @Autowired

    public LoginController(LoginService service) {
        this.service = service;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public String login(HttpSession session, Model model, @RequestParam String name, @RequestParam String password) {
        boolean isValidUser = service.validateUser(name, password);
        if (!isValidUser) {
            model.addAttribute("errorMessage", "Invalid Credentials");
            return "/";
        }
        session.setAttribute("user", name);
        return "main";
    }

}
