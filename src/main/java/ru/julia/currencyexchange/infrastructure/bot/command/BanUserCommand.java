package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.BanUserKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.BanUserCallbackHandler;

import java.util.List;

@Component
public class BanUserCommand extends AbstractCommandHandler {
    private final UserService userService;
    private final BanUserKeyboardBuilder keyboardBuilder;
    private final BanUserCallbackHandler callbackHandler;

    public BanUserCommand(MessageConverter messageConverter,
                          UserService userService,
                          BanUserKeyboardBuilder keyboardBuilder,
                          BanUserCallbackHandler callbackHandler) {
        super(messageConverter);
        this.userService = userService;
        this.keyboardBuilder = keyboardBuilder;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        User user = userService.findUserByChatId(chatId);
        if (!isAccessible(user)) {
            return new SendMessage(chatId, messageConverter.resolve("command.banUser.no_access"));
        }
        List<User> users = userService.findAllUsers(null);
        if (users.isEmpty()) {
            return new SendMessage(chatId, messageConverter.resolve("command.banUser.empty"));
        }
        var keyboard = keyboardBuilder.buildEmailKeyboard(users);
        String messageText = messageConverter.resolve("command.banUser.select_email");
        SendMessage sendMessage = new SendMessage(chatId, messageText)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
        return sendMessage;
    }

    @Override
    public String getCommand() {
        return "/banUser";
    }

    @Override
    public String getDescription() {
        return "command.banUser.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "ADMIN".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }

    public BanUserCallbackHandler getCallbackHandler() {
        return callbackHandler;
    }
} 