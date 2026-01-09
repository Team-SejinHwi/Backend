package com.neo.rental.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/test")
    public String test() {
        return "Hello World! 통신 성공!";
    }
}
