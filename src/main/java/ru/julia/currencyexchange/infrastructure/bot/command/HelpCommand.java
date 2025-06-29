package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;

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
            if (userService.existsByChatId(chatId)) {
                User user = userService.findUserByChatId(chatId);

                if (user.isBanned()) {
                    return new SendMessage(chatId, messageConverter.resolve("command.help.banned_message"));
                }

                if (user.isVerified()) {
                    String role = getUserRole(user);

                    if ("ADMIN".equals(role)) {
                        return new SendMessage(chatId, messageConverter.resolve("command.help.admin_help_message"));
                    } else {
                        return new SendMessage(chatId, messageConverter.resolve("command.help.user_help_message"));
                    }
                } else {
                    return new SendMessage(chatId, messageConverter.resolve("command.help.unverified_help_message"));
                }
            } else {
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