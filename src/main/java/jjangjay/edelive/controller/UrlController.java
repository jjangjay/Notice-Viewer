package jjangjay.edelive.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class UrlController {
    @GetMapping("/login")
    public String loginPage() {
        return "login.html";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "main.html";
    }

    @GetMapping("/")
    public String indexPage() {
        return "login.html";
    }
}