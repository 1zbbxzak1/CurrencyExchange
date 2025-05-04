package ru.julia.currencyexchange.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.domain.model.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/register")
    public User createUserWithSettings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String preferredCurrency) {
        return authService.createUserWithSettings(username, password, preferredCurrency);
    }
}
