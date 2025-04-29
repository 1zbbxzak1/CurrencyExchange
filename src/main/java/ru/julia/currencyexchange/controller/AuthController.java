package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User createUserWithSettings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String preferredCurrency) {
        return authService.createUserWithSettings(username, password, preferredCurrency);
    }
}
