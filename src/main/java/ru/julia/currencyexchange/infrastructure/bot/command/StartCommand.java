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
public class StartCommand extends AbstractCommandHandler {
    private final UserService userService;

    public StartCommand(MessageConverter messageConverter, UserService userService) {
        super(messageConverter);
        this.userService = userService;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String firstName = update.message().chat().firstName();

        try {
            // Проверяем, существует ли пользователь
            if (userService.existsByChatId(chatId)) {
                // Пользователь существует, проверяем его статус
                User user = userService.findUserByChatId(chatId);
                
                if (user.isBanned()) {
                    return new SendMessage(chatId, messageConverter.resolve("command.start.banned_message"));
                }
                
                if (user.isVerified()) {
                    // Пользователь верифицирован и не забанен - показываем приветствие
                    userService.updateUsernameIfChanged(chatId, update.message().chat().username());
                    return new SendMessage(
                            chatId, messageConverter.resolve("command.start.welcome_back_message", Map.of("user_name", firstName)));
                } else {
                    // Пользователь не верифицирован - предлагаем завершить регистрацию
                    return new SendMessage(
                            chatId, messageConverter.resolve("command.start.not_verified_message"));
                }
            } else {
                // Пользователь не существует - предлагаем регистрацию
                userService.updateUsernameIfChanged(chatId, update.message().chat().username());
                return new SendMessage(
                        chatId, messageConverter.resolve("command.start.start_message", Map.of("user_name", firstName)));
            }
        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.start.error_message"));
        }
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "command.start.description";
    }
}
