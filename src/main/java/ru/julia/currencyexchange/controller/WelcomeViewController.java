package ru.julia.currencyexchange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeViewController {
    @GetMapping("/")
    public String welcome() {
        return "index";
    }
}
