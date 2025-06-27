package ru.julia.currencyexchange.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на подтверждение email")
public class VerifyRequest {
    @Schema(description = "Email пользователя", example = "user@example.com")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    private String email;
    
    @Schema(description = "Код подтверждения", example = "123456")
    @NotBlank(message = "Код подтверждения не может быть пустым")
    @Size(min = 6, max = 6, message = "Код подтверждения должен содержать 6 символов")
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
