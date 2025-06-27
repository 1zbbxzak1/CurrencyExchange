package ru.julia.currencyexchange.application.dto.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на поиск истории конвертаций по валюте и дате")
public class CurrencyHistoryRequest {
    @Schema(description = "Дата в формате YYYY-MM-DD", example = "2024-01-01")
    @NotBlank(message = "Дата не может быть пустой")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Дата должна быть в формате YYYY-MM-DD")
    private String timestamp;

    public CurrencyHistoryRequest() {
    }

    public CurrencyHistoryRequest(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
} 