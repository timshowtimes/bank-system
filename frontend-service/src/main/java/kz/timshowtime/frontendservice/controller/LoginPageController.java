package kz.timshowtime.frontendservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {
    @GetMapping("/login")
    public String index() {
        return "login";
    }

    @GetMapping("/register-page")
    public String registerPage() {
        return "register";
    }
}
