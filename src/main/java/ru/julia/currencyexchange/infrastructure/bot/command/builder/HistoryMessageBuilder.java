package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;
import java.util.Map;

@Component
public class HistoryMessageBuilder {
    private final MessageConverter messageConverter;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;

    public HistoryMessageBuilder(MessageConverter messageConverter,
                                 CurrencyEmojiUtils currencyEmojiUtils,
                                 CurrencyFormatUtils currencyFormatUtils) {
        this.messageConverter = messageConverter;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
    }

    public String buildHistoryMessage(List<CurrencyConversion> conversions, int page, boolean useCompactFormat, int conversionsPerPage) {
        return buildConversionMessage(conversions, page, useCompactFormat, conversionsPerPage, 
                "command.history.title", "command.history.subtitle", null, 
                "command.history.pagination");
    }

    public String buildFindByDateMessage(List<CurrencyConversion> conversions, int page, boolean useCompactFormat, int conversionsPerPage, String dateStr) {
        String formattedDate = formatDate(dateStr);
        return buildConversionMessage(conversions, page, useCompactFormat, conversionsPerPage, 
                "command.findByDate.title", "command.findByDate.subtitle", formattedDate, 
                "command.findByDate.pagination");
    }

    private String buildConversionMessage(List<CurrencyConversion> conversions, int page, boolean useCompactFormat, int conversionsPerPage,
                                        String titleKey, String subtitleKey, String dateParam, String paginationKey) {
        StringBuilder message = new StringBuilder();

        message.append(messageConverter.resolve(titleKey)).append(Constants.LINE_SEPARATOR);
        
        if (dateParam != null) {
            message.append(messageConverter.resolve(subtitleKey, Map.of("date", dateParam))).append(Constants.LINE_SEPARATOR);
        } else {
            message.append(messageConverter.resolve(subtitleKey)).append(Constants.LINE_SEPARATOR);
        }
        message.append(Constants.LINE_SEPARATOR);

        int startIndex = page * conversionsPerPage;
        int endIndex = Math.min(startIndex + conversionsPerPage, conversions.size());
        int totalPages = (conversions.size() - 1) / conversionsPerPage + 1;

        for (int i = startIndex; i < endIndex; i++) {
            CurrencyConversion conversion = conversions.get(i);

            String conversionLine;
            if (useCompactFormat) {
                conversionLine = messageConverter.resolve("command.history.conversion_format_compact",
                        Map.of(
                                "from_emoji", currencyEmojiUtils.getCurrencyEmoji(conversion.getSourceCurrency().getCode()),
                                "from_code", conversion.getSourceCurrency().getCode(),
                                "to_emoji", currencyEmojiUtils.getCurrencyEmoji(conversion.getTargetCurrency().getCode()),
                                "to_code", conversion.getTargetCurrency().getCode(),
                                "amount", currencyFormatUtils.formatAmount(conversion.getAmount()),
                                "result", currencyFormatUtils.formatAmount(conversion.getConvertedAmount()),
                                "date", conversion.getFormattedTimestamp()
                        ));
            } else {
                conversionLine = messageConverter.resolve("command.history.conversion_format",
                        Map.of(
                                "from_emoji", currencyEmojiUtils.getCurrencyEmoji(conversion.getSourceCurrency().getCode()),
                                "from_code", conversion.getSourceCurrency().getCode(),
                                "to_emoji", currencyEmojiUtils.getCurrencyEmoji(conversion.getTargetCurrency().getCode()),
                                "to_code", conversion.getTargetCurrency().getCode(),
                                "amount", currencyFormatUtils.formatAmount(conversion.getAmount()),
                                "result", currencyFormatUtils.formatAmount(conversion.getConvertedAmount()),
                                "date", conversion.getFormattedTimestamp()
                        ));
            }
            message.append(conversionLine).append(Constants.LINE_SEPARATOR);

            if (!useCompactFormat && i < endIndex - 1) {
                message.append(Constants.LINE_SEPARATOR);
            }
        }

        message.append(Constants.LINE_SEPARATOR).append(Constants.LINE_SEPARATOR);
        message.append(messageConverter.resolve(paginationKey + ".page_info",
                Map.of("current", String.valueOf(page + 1), "total", String.valueOf(totalPages))));
        message.append(" | ").append(messageConverter.resolve(paginationKey + ".total_conversions",
                Map.of("count", String.valueOf(conversions.size()))));

        if (useCompactFormat) {
            message.append(" | ").append(messageConverter.resolve(paginationKey + ".compact_mode"));
        }

        return message.toString();
    }

    private String formatDate(String dateStr) {
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
} 