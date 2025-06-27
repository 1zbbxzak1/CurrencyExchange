package ru.julia.currencyexchange.application.dto.currency;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на поиск истории конвертаций по валюте и дате")
public class CurrencyHistoryRequest {
    @Schema(description = "Код валюты", example = "USD")
    private String currencyCode;
    
    @Schema(description = "Дата в формате YYYY-MM-DD", example = "2024-01-01")
    private String timestamp;

    public CurrencyHistoryRequest() {
    }

    public CurrencyHistoryRequest(String currencyCode, String timestamp) {
        this.currencyCode = currencyCode;
        this.timestamp = timestamp;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
} 