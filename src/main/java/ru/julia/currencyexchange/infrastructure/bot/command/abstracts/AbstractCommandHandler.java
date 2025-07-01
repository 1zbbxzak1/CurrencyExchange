package ru.julia.currencyexchange.infrastructure.bot.command.abstracts;

import com.pengrad.telegrambot.model.BotCommand;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;
import ru.julia.currencyexchange.domain.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractCommandHandler implements BotCommandHandler {
    protected final MessageConverter messageConverter;

    protected AbstractCommandHandler(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public BotCommand toBotCommand() {
        return new BotCommand(getCommand(), messageConverter.resolve(getDescription()));
    }

    protected String getUserRole(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return roles.stream()
                .min(java.util.Comparator.comparing(r -> "ADMIN".equals(r) ? 0 : 1))
                .orElse("USER");
    }
}
