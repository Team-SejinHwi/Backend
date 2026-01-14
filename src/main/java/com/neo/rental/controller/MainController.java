package com.neo.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/rental/main")
    public String mainPage() {
        return "main"; // templates/main.html 파일을 찾아감
    }
}