package ru.julia.currencyexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.dto.auth.AuthResponse;
import ru.julia.currencyexchange.application.dto.auth.RegisterRequest;
import ru.julia.currencyexchange.application.dto.auth.VerifyRequest;
import ru.julia.currencyexchange.application.dto.common.ApiResponseDto;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.util.ValidationUtil;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Auth Controller", description = "API для аутентификации и регистрации пользователей")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя и отправляет код подтверждения на email")
    @ApiResponse(responseCode = "200", description = "Код подтверждения отправлен")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    public ResponseEntity<ApiResponseDto<AuthResponse>> createUserWithSettings(@Valid @RequestBody RegisterRequest request) {
        ValidationUtil.validateNotEmpty(request.getEmail(), "Email");
        ValidationUtil.validateNotEmpty(request.getPassword(), "Пароль");
        ValidationUtil.validateNotEmpty(request.getUsername(), "Username");
        ValidationUtil.validateNotNull(request.getChatId(), "ChatId");

        authService.createUserWithVerificationCode(
                request.getChatId(),
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        AuthResponse authResponse = new AuthResponse(
                "Код подтверждения отправлен на email: " + request.getEmail(),
                true
        );

        return ResponseEntity.ok(ApiResponseDto.success("Регистрация выполнена успешно", authResponse));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/verify")
    @Operation(summary = "Подтверждение email", description = "Подтверждает email пользователя с помощью кода")
    @ApiResponse(responseCode = "200", description = "Email подтвержден")
    @ApiResponse(responseCode = "400", description = "Неверный код подтверждения")
    public ResponseEntity<ApiResponseDto<AuthResponse>> verifyUser(@Valid @RequestBody VerifyRequest request) {
        ValidationUtil.validateNotEmpty(request.getEmail(), "Email");
        ValidationUtil.validateNotEmpty(request.getCode(), "Код подтверждения");

        boolean result = userService.verifyUserCode(request.getEmail(), request.getCode());

        if (result) {
            AuthResponse authResponse = new AuthResponse("Почта подтверждена!", true);

            return ResponseEntity.ok(ApiResponseDto.success("Email подтвержден", authResponse));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Неверный код подтверждения", 400));
        }
    }
}
