package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;

@Component
public class PaginationKeyboardBuilder {
    private final MessageConverter messageConverter;

    public PaginationKeyboardBuilder(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildPaginationKeyboard(int totalCurrencies,
                                                        int currentPage,
                                                        int currenciesPerPage) {
        PaginationConfig config = new PaginationConfig(
                "command.currencies.pagination",
                "currencies_page_"
        );
        return buildPaginationKeyboard(totalCurrencies, currentPage, currenciesPerPage, config);
    }

    private InlineKeyboardMarkup buildPaginationKeyboard(int totalItems,
                                                         int currentPage,
                                                         int itemsPerPage,
                                                         PaginationConfig config) {
        int totalPages = (totalItems - 1) / itemsPerPage + 1;

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];

        if (currentPage > 0) {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".previous"))
                    .callbackData(config.callbackPrefix + (currentPage - 1));
        } else {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".last"))
                    .callbackData(config.callbackPrefix + (totalPages - 1));
        }

        if (currentPage < totalPages - 1) {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".next"))
                    .callbackData(config.callbackPrefix + (currentPage + 1));
        } else {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".first"))
                    .callbackData(config.callbackPrefix + "0");
        }

        keyboard.addRow(buttons);
        return keyboard;
    }
} 