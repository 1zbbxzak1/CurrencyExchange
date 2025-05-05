package ru.julia.currencyexchange.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.domain.model.User;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User createUserWithSettings(
            @RequestParam @NotBlank(message = "Username is required") String username,
            @RequestParam @NotBlank(message = "Password is required") String password,
            @RequestParam @NotBlank(message = "Preferred currency is required") String preferredCurrency) {
        return authService.createUserWithSettings(username, password, preferredCurrency);
    }
}
