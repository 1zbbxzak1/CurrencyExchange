package ru.julia.currencyexchange.infrastructure.bot.command.interfaces;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface BotCommandHandler {
    SendMessage handle(Update update);

    default boolean matches(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return false;
        }

        String[] words = update.message().text().trim().split("\\s+");

        return words.length > 0 && words[0].equals(getCommand());
    }

    String getCommand();

    default BotCommand toBotCommand() {
        return new BotCommand(getCommand(), getDescription());
    }

    String getDescription();
}
