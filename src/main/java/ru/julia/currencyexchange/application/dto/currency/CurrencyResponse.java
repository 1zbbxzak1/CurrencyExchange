package ru.julia.currencyexchange.application.dto.currency;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ответ с информацией о валюте")
public class CurrencyResponse {
    @Schema(description = "ID валюты", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Код валюты", example = "USD")
    private String code;
    
    @Schema(description = "Название валюты", example = "Доллар США")
    private String name;
    
    @Schema(description = "Курс валюты", example = "75.50")
    private BigDecimal exchangeRate;
    
    @Schema(description = "Время последнего обновления", example = "2024-01-01T12:00:00")
    private LocalDateTime lastUpdated;

    public CurrencyResponse() {
    }

    public CurrencyResponse(String id, String code, String name, BigDecimal exchangeRate, LocalDateTime lastUpdated) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.exchangeRate = exchangeRate;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 