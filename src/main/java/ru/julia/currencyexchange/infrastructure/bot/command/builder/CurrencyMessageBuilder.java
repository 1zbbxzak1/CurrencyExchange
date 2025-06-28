package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;

import java.util.List;
import java.util.Map;

@Component
public class CurrencyMessageBuilder {
    private final MessageConverter messageConverter;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;

    public CurrencyMessageBuilder(MessageConverter messageConverter,
                                  CurrencyEmojiUtils currencyEmojiUtils,
                                  CurrencyFormatUtils currencyFormatUtils) {
        this.messageConverter = messageConverter;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
    }

    public String buildCurrenciesMessage(List<Currency> currencies, int page, boolean useCompactFormat, int currenciesPerPage) {
        StringBuilder message = new StringBuilder();

        message.append(messageConverter.resolve("command.currencies.title")).append("\n");
        message.append(messageConverter.resolve("command.currencies.subtitle")).append("\n");
        message.append("\n");

        int startIndex = page * currenciesPerPage;
        int endIndex = Math.min(startIndex + currenciesPerPage, currencies.size());
        int totalPages = (currencies.size() - 1) / currenciesPerPage + 1;

        for (int i = startIndex; i < endIndex; i++) {
            Currency currency = currencies.get(i);

            String currencyLine;
            if (useCompactFormat) {
                currencyLine = messageConverter.resolve("command.currencies.currency_format_compact",
                        Map.of(
                                "code", currencyEmojiUtils.getCurrencyEmoji(currency.getCode()) + " *" + currency.getCode() + "*",
                                "name", currency.getName(),
                                "rate", currencyFormatUtils.formatExchangeRate(currency.getExchangeRate())
                        ));
            } else {
                currencyLine = messageConverter.resolve("command.currencies.currency_format",
                        Map.of(
                                "code", currencyEmojiUtils.getCurrencyEmoji(currency.getCode()) + " *" + currency.getCode() + "*",
                                "name", currency.getName(),
                                "rate", currencyFormatUtils.formatExchangeRate(currency.getExchangeRate())
                        ));
            }
            message.append(currencyLine).append("\n");

            if (!useCompactFormat && i < endIndex - 1) {
                message.append("\n");
            }
        }

        message.append("\n").append("\n");
        message.append("📊 *Страница ").append(page + 1).append(" из ").append(totalPages).append("*");
        message.append(" | Всего валют: *").append(currencies.size()).append("*");

        if (useCompactFormat) {
            message.append(" | 🚀 Компактный режим");
        }

        return message.toString();
    }
} 