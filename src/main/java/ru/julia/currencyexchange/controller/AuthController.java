package ru.julia.currencyexchange.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.dto.auth.RegisterRequest;
import ru.julia.currencyexchange.application.dto.auth.VerifyRequest;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> createUserWithSettings(@RequestBody RegisterRequest request) {
        authService.createUserWithVerificationCode(request.getEmail(), request.getPassword());

        return ResponseEntity.ok("Код подтверждения отправлен на email: " + request.getEmail());
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestBody VerifyRequest request) {
        boolean result = userService.verifyUserCode(request.getEmail(), request.getCode());
        if (result) {
            return ResponseEntity.ok("Почта подтверждена!");
        } else {
            return ResponseEntity.badRequest().body("Неверный код");
        }
    }
}
