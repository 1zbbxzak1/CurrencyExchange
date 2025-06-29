package ru.julia.currencyexchange.application.dto.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на конвертацию валюты")
public class ConvertRequest {
    @Schema(description = "Код исходной валюты", example = "USD")
    @NotBlank(message = "Код исходной валюты не может быть пустым")
    private String fromCurrency;

    @Schema(description = "Код целевой валюты", example = "EUR")
    @NotBlank(message = "Код целевой валюты не может быть пустым")
    private String toCurrency;

    @Schema(description = "Сумма для конвертации", example = "100.50")
    @NotNull(message = "Сумма не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
