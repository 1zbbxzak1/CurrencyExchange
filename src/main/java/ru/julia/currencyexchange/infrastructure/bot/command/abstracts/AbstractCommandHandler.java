package ru.julia.currencyexchange.infrastructure.bot.command.abstracts;

import com.pengrad.telegrambot.model.BotCommand;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

public abstract class AbstractCommandHandler implements BotCommandHandler {
    protected final MessageConverter messageConverter;

    protected AbstractCommandHandler(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public BotCommand toBotCommand() {
        return new BotCommand(getCommand(), messageConverter.resolve(getDescription()));
    }
}
