package ru.julia.currencyexchange.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "currency_conversions")
public class CurrencyConversion {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String sourceCurrency;

    @Column(nullable = false)
    private String targetCurrency;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private double convertedAmount;

    @Column(nullable = false)
    private double conversionRate;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public CurrencyConversion(User user, String sourceCurrency, String targetCurrency,
                              double amount, double convertedAmount, double conversionRate) {
        this.user = user;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.conversionRate = conversionRate;
        this.timestamp = LocalDateTime.now();
    }

    public CurrencyConversion() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Метод для форматированного вывода даты
    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }
}
