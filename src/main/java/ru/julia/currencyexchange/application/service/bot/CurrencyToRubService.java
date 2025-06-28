package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;

import java.util.List;

@Service
public class CurrencyToRubService {
    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;

    public CurrencyToRubService(CurrencyExchangeService currencyExchangeService,
                                CurrencyEmojiUtils currencyEmojiUtils,
                                CurrencyFormatUtils currencyFormatUtils) {
        this.currencyExchangeService = currencyExchangeService;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
    }

    public Currency getCurrencyByCode(String currencyCode) {
        return currencyExchangeService.getCurrencyByCode(currencyCode);
    }

    public boolean hasCurrencies() {
        List<Currency> currencies = getAllCurrencies();
        return currencies != null && !currencies.isEmpty();
    }

    public List<Currency> getAllCurrencies() {
        return currencyExchangeService.getAllCurrencies();
    }

    public List<Currency> getPopularCurrencies() {
        String[] popularCodes = {"USD", "EUR", "GBP", "JPY", "CNY", "CHF", "CAD", "AUD"};
        return getAllCurrencies().stream()
                .filter(currency -> List.of(popularCodes).contains(currency.getCode()))
                .toList();
    }

    public List<Currency> getOtherCurrencies() {
        String[] popularCodes = {"USD", "EUR", "GBP", "JPY", "CNY", "CHF", "CAD", "AUD"};
        return getAllCurrencies().stream()
                .filter(currency -> !List.of(popularCodes).contains(currency.getCode()))
                .toList();
    }

    public String buildCurrencyToRubMessage(Currency currency) {

        String message = "💱 *Курс валюты к рублю*\n\n" +
                currencyEmojiUtils.getCurrencyEmoji(currency.getCode()) +
                " *" + currency.getCode() + "* - " +
                currency.getName() + "\n\n" +
                "📊 *Курс:* " +
                currencyFormatUtils.formatExchangeRate(currency.getExchangeRate()) +
                " ₽\n\n" +
                "🕐 *Обновлено:* " +
                currency.getLastUpdated().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) +
                "\n\n" +
                "💡 *Пример:* 1 " + currency.getCode() +
                " = " + currencyFormatUtils.formatExchangeRate(currency.getExchangeRate()) +
                " ₽";

        return message;
    }
} 