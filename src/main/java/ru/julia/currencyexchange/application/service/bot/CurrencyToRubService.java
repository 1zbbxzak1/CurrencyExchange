package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;
import java.util.Map;

@Service
public class CurrencyToRubService {

    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;
    private final MessageConverter messageConverter;

    public CurrencyToRubService(CurrencyExchangeService currencyExchangeService,
                                CurrencyEmojiUtils currencyEmojiUtils,
                                CurrencyFormatUtils currencyFormatUtils,
                                MessageConverter messageConverter) {
        this.currencyExchangeService = currencyExchangeService;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
        this.messageConverter = messageConverter;
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
        return getAllCurrencies().stream()
                .filter(currency -> Constants.POPULAR_CURRENCIES.contains(currency.getCode()))
                .toList();
    }

    public String buildCurrencyToRubMessage(Currency currency) {

        String message = messageConverter.resolve("command.currencyToRub.result.title") + Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.result.currency_info",
                        Map.of("emoji", currencyEmojiUtils.getCurrencyEmoji(currency.getCode()),
                                "code", currency.getCode(),
                                "name", currency.getName())) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.result.rate",
                        Map.of("rate", currencyFormatUtils.formatExchangeRate(currency.getExchangeRate()))) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.result.updated",
                        Map.of("date", currency.getLastUpdated().format(Constants.DATE_FORMATTER))) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.result.example",
                        Map.of("code", currency.getCode(),
                                "rate", currencyFormatUtils.formatExchangeRate(currency.getExchangeRate())));

        return message;
    }
} 