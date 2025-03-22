package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.dto.CurrencyConversion;
import ru.julia.currencyexchange.service.CurrencyExchangeService;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyExchangeService converterService;

    public CurrencyController(CurrencyExchangeService converterService) {
        this.converterService = converterService;
    }

    @GetMapping("/convert")
    public double convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount) {
        return converterService.convert(from.toUpperCase(), to.toUpperCase(), amount);
    }

    @GetMapping("/history")
    public List<CurrencyConversion> getConversionHistory() {
        return converterService.getConversionHistory();
    }
}
