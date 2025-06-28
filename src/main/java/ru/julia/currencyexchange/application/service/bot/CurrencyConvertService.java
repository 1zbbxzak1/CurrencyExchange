package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CurrencyConvertService {
    private static final Set<String> POPULAR_CURRENCIES = Set.of("USD", "EUR", "GBP", "JPY", "CNY", "CHF", "CAD", "AUD", "RUB");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999");
    private static final int CONVERSION_SCALE = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;
    private final MessageConverter messageConverter;

    public CurrencyConvertService(CurrencyExchangeService currencyExchangeService,
                                  CurrencyEmojiUtils currencyEmojiUtils,
                                  CurrencyFormatUtils currencyFormatUtils,
                                  MessageConverter messageConverter) {
        this.currencyExchangeService = currencyExchangeService;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
        this.messageConverter = messageConverter;
    }

    public boolean hasCurrencies() {
        return !getAllCurrencies().isEmpty();
    }

    public List<Currency> getAllCurrencies() {
        return currencyExchangeService.getAllCurrencies();
    }

    public List<Currency> getPopularCurrencies() {
        return getAllCurrencies().stream()
                .filter(currency -> POPULAR_CURRENCIES.contains(currency.getCode()))
                .toList();
    }

    public BigDecimal convertCurrency(String fromCurrencyCode, String toCurrencyCode, BigDecimal amount) {
        Currency fromCurrency = getCurrencyByCode(fromCurrencyCode);
        Currency toCurrency = getCurrencyByCode(toCurrencyCode);

        validateCurrencies(fromCurrency, toCurrency);

        BigDecimal amountInRub = amount.multiply(fromCurrency.getExchangeRate());
        return amountInRub.divide(toCurrency.getExchangeRate(), CONVERSION_SCALE, RoundingMode.HALF_UP);
    }

    public Currency getCurrencyByCode(String currencyCode) {
        return currencyExchangeService.getCurrencyByCode(currencyCode);
    }

    public String buildConversionMessage(String fromCurrencyCode, String toCurrencyCode,
                                         BigDecimal amount, BigDecimal result) {
        Currency fromCurrency = getCurrencyByCode(fromCurrencyCode);
        Currency toCurrency = getCurrencyByCode(toCurrencyCode);

        StringBuilder message = new StringBuilder();
        message.append(messageConverter.resolve("command.convert.result.title")).append("\n\n");
        
        message.append(messageConverter.resolve("command.convert.result.from",
                Map.of("emoji", currencyEmojiUtils.getCurrencyEmoji(fromCurrency.getCode()),
                       "code", fromCurrency.getCode(),
                       "name", fromCurrency.getName()))).append("\n");
        
        message.append(messageConverter.resolve("command.convert.result.amount",
                Map.of("amount", currencyFormatUtils.formatAmount(amount),
                       "code", fromCurrency.getCode(),
                       "rate", currencyFormatUtils.formatExchangeRate(fromCurrency.getExchangeRate())))).append("\n\n");
        
        message.append(messageConverter.resolve("command.convert.result.to",
                Map.of("emoji", currencyEmojiUtils.getCurrencyEmoji(toCurrency.getCode()),
                       "code", toCurrency.getCode(),
                       "name", toCurrency.getName()))).append("\n");
        
        message.append(messageConverter.resolve("command.convert.result.rate",
                Map.of("code", toCurrency.getCode(),
                       "rate", currencyFormatUtils.formatExchangeRate(toCurrency.getExchangeRate())))).append("\n\n");
        
        message.append(messageConverter.resolve("command.convert.result.result",
                Map.of("amount", currencyFormatUtils.formatAmount(amount),
                       "from_code", fromCurrency.getCode(),
                       "result", currencyFormatUtils.formatAmount(result),
                       "to_code", toCurrency.getCode()))).append("\n\n");
        
        message.append(messageConverter.resolve("command.convert.result.updated",
                Map.of("date", fromCurrency.getLastUpdated().format(DATE_FORMATTER))));
        
        return message.toString();
    }

    public boolean isRub(String currencyCode) {
        return "RUB".equalsIgnoreCase(currencyCode);
    }

    public boolean isValidAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            return amount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(MAX_AMOUNT) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public BigDecimal parseAmount(String amountStr) {
        return new BigDecimal(amountStr.replace(",", "."));
    }

    private void validateCurrencies(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException(messageConverter.resolve("command.convert.validation.currency_not_found"));
        }
    }
} 