package ru.julia.currencyexchange.application.dto.currency;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ответ с информацией о конвертации валюты")
public class CurrencyConversionResponse {
    @Schema(description = "ID конвертации", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;
    
    @Schema(description = "Исходная валюта", example = "USD")
    private String sourceCurrency;
    
    @Schema(description = "Целевая валюта", example = "EUR")
    private String targetCurrency;
    
    @Schema(description = "Исходная сумма", example = "100.50")
    private BigDecimal amount;
    
    @Schema(description = "Конвертированная сумма", example = "85.25")
    private BigDecimal convertedAmount;
    
    @Schema(description = "Курс конвертации", example = "0.8525")
    private BigDecimal conversionRate;
    
    @Schema(description = "Время конвертации", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;

    public CurrencyConversionResponse() {
    }

    public CurrencyConversionResponse(String id, String userId, String sourceCurrency, String targetCurrency, 
                                    BigDecimal amount, BigDecimal convertedAmount, BigDecimal conversionRate, 
                                    LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.conversionRate = conversionRate;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 