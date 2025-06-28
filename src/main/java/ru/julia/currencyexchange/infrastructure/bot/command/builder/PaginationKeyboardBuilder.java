package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;

@Component
public class PaginationKeyboardBuilder {

    public InlineKeyboardMarkup buildPaginationKeyboard(int totalCurrencies, int currentPage, int currenciesPerPage) {
        int totalPages = (totalCurrencies - 1) / currenciesPerPage + 1;

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];

        if (currentPage > 0) {
            buttons[0] = new InlineKeyboardButton("⬅️ Предыдущая")
                    .callbackData("currencies_page_" + (currentPage - 1));
        } else {
            buttons[0] = new InlineKeyboardButton("⬅️ Последняя")
                    .callbackData("currencies_page_" + (totalPages - 1));
        }

        if (currentPage < totalPages - 1) {
            buttons[1] = new InlineKeyboardButton("Следующая ➡️")
                    .callbackData("currencies_page_" + (currentPage + 1));
        } else {
            buttons[1] = new InlineKeyboardButton("Первая ➡️")
                    .callbackData("currencies_page_0");
        }

        keyboard.addRow(buttons);

        return keyboard;
    }
} 