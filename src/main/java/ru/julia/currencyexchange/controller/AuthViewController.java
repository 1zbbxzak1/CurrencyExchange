package ru.julia.currencyexchange.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @PreAuthorize("isAnonymous()")
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/auth")
    public String auth() {
        return "auth";
    }
}
