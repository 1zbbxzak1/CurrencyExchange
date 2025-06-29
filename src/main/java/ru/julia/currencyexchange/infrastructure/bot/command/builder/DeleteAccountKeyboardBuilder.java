package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

@Component
public class DeleteAccountKeyboardBuilder {

    public InlineKeyboardMarkup buildDeleteAccountKeyboard() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[][]{
                        {new InlineKeyboardButton("Удалить аккаунт").callbackData(Constants.CALLBACK_CONFIRM),
                         new InlineKeyboardButton("Отмена").callbackData(Constants.CALLBACK_CANCEL)}
                }
        );
    }
} 