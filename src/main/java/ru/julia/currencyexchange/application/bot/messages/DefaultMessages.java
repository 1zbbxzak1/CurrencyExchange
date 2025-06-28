package ru.julia.currencyexchange.application.bot.messages;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.infrastructure.bot.command.StartCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultMessages {
    private final MessageConverter messageConverter;
    private final UserService userService;

    private final List<BotCommandHandler> botCommands = new ArrayList<>();

    public DefaultMessages(MessageConverter messageConverter,
                           UserService userService) {
        this.messageConverter = messageConverter;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        botCommands.add(new StartCommand(messageConverter, userService));
    }

    public void addCommand(BotCommandHandler command) {
        botCommands.add(command);
    }

    public SendMessage sendMessage(Update update) {
        if (update.message() == null) {
            return null;
        }

        Long chatId = update.message().chat().id();
        String message = update.message().text();

        // Проверяем команды
        for (BotCommandHandler botCommand : botCommands) {
            if (botCommand.matches(update)) {
                return botCommand.handle(update);
            }
        }

        return new SendMessage(
                chatId,
                messageConverter.resolve(
                        "message.unknown_command", Map.of("command", message != null ? message : "None")));
    }
}
