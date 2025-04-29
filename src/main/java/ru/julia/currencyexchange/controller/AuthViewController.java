package ru.julia.currencyexchange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/auth")
    public String auth() {
        return "auth";
    }
}
