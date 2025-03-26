package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.entity.CurrencyConversion;
import ru.julia.currencyexchange.service.CurrencyExchangeService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyExchangeService converterService;

    public CurrencyController(CurrencyExchangeService converterService) {
        this.converterService = converterService;
    }

    @GetMapping
    public List<Currency> updateCurrencyRates() {
        return converterService.updateCurrencyRates();
    }

    @GetMapping("/convert")
    public CurrencyConversion convertCurrency(
            @RequestParam String userId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount) {
        return converterService.convert(userId, from.toUpperCase(), to.toUpperCase(), amount);
    }

    @GetMapping("/history")
    public List<CurrencyConversion> getUserHistory(@RequestParam String userId) {
        return converterService.getUserHistory(userId);
    }

    @GetMapping("/find")
    public Optional<CurrencyConversion> findBySourceCurrencyAndTimestamp(
            @RequestParam String sourceCurrency,
            @RequestParam String timestamp) {
        return converterService.findBySourceCurrencyAndTimestamp(sourceCurrency.toUpperCase(), timestamp);
    }

    @GetMapping("/conversion-range")
    public Optional<List<CurrencyConversion>> getConversionByAmountRange(
            @RequestParam Double minAmount,
            @RequestParam Double maxAmount) {
        return converterService.getConversionByAmountRange(minAmount, maxAmount);
    }
}
