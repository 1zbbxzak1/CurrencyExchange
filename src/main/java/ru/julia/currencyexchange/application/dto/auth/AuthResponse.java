package ru.julia.currencyexchange.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации")
public class AuthResponse {
    @Schema(description = "Сообщение", example = "Код подтверждения отправлен на email: user@example.com")
    private String message;
    
    @Schema(description = "Статус операции", example = "true")
    private boolean success;

    public AuthResponse() {
    }

    public AuthResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
} 