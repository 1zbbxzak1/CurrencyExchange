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

    public InlineKeyboardMarkup buildPaginationKeyboard(int totalCurrencies, int currentPage, int currenciesPerPage) {
        int totalPages = (totalCurrencies - 1) / currenciesPerPage + 1;

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];

        if (currentPage > 0) {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve("command.currencies.pagination.previous"))
                    .callbackData("currencies_page_" + (currentPage - 1));
        } else {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve("command.currencies.pagination.last"))
                    .callbackData("currencies_page_" + (totalPages - 1));
        }

        if (currentPage < totalPages - 1) {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve("command.currencies.pagination.next"))
                    .callbackData("currencies_page_" + (currentPage + 1));
        } else {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve("command.currencies.pagination.first"))
                    .callbackData("currencies_page_0");
        }

        keyboard.addRow(buttons);

        return keyboard;
    }
} 