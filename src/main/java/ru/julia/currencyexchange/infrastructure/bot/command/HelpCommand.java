package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;

import java.util.Map;

@Component
public class HelpCommand extends AbstractCommandHandler {
    private final UserService userService;

    public HelpCommand(MessageConverter messageConverter, UserService userService) {
        super(messageConverter);
        this.userService = userService;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();

        try {
            // Проверяем статус пользователя
            if (userService.existsByChatId(chatId)) {
                User user = userService.findUserByChatId(chatId);
                
                if (user.isBanned()) {
                    return new SendMessage(chatId, messageConverter.resolve("command.help.banned_message"));
                }
                
                if (user.isVerified()) {
                    // Пользователь верифицирован - показываем все команды
                    return new SendMessage(chatId, messageConverter.resolve("command.help.verified_help_message"));
                } else {
                    // Пользователь не верифицирован - показываем ограниченный список
                    return new SendMessage(chatId, messageConverter.resolve("command.help.unverified_help_message"));
                }
            } else {
                // Пользователь не зарегистрирован - показываем базовые команды
                return new SendMessage(chatId, messageConverter.resolve("command.help.unregistered_help_message"));
            }
        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.help.error_message"));
        }
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "command.help.description";
    }
} 