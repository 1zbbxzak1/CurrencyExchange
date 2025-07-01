package ru.julia.currencyexchange.application.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ответ с ошибкой")
public class ErrorResponse {
    @Schema(description = "Время возникновения ошибки", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP статус код", example = "400")
    private int status;
    
    @Schema(description = "Тип ошибки", example = "Bad Request")
    private String error;
    
    @Schema(description = "Сообщение об ошибке", example = "Неверный формат данных")
    private String message;
    
    @Schema(description = "Путь запроса", example = "/api/currency/convert")
    private String path;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
} 