package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.math.BigDecimal;
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
            @RequestParam BigDecimal amount) {
        return converterService.convert(userId, from.toUpperCase(), to.toUpperCase(), amount);
    }

    @GetMapping("/history")
    public List<CurrencyConversion> getUserHistory(@RequestParam String userId) {
        return converterService.getUserHistory(userId);
    }

    @GetMapping("/history/find")
    public List<CurrencyConversion> findByCurrencyCodeAndDate(
            @RequestParam String currencyCode,
            @RequestParam String timestamp) {
        return converterService.findByCurrencyCodeAndDate(currencyCode.toUpperCase(), timestamp);
    }
}
