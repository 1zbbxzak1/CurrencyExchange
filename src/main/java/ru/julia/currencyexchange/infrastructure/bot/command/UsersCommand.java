package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.UserMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.UsersCallbackHandler;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;

@Component
public class UsersCommand extends AbstractCommandHandler {
    private final UserService userService;
    private final UserMessageBuilder userMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;
    private final UsersCallbackHandler usersCallbackHandler;

    public UsersCommand(MessageConverter messageConverter,
                        UserService userService,
                        UserMessageBuilder userMessageBuilder,
                        PaginationKeyboardBuilder paginationKeyboardBuilder,
                        UsersCallbackHandler usersCallbackHandler) {
        super(messageConverter);
        this.userService = userService;
        this.userMessageBuilder = userMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
        this.usersCallbackHandler = usersCallbackHandler;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        User user = userService.findUserByChatId(chatId);
        if (!isAccessible(user)) {
            return new SendMessage(chatId, messageConverter.resolve("command.users.no_access"));
        }
        List<User> users = userService.findAllUsers(null);
        if (users.isEmpty()) {
            return new SendMessage(chatId, messageConverter.resolve("command.users.empty"));
        }
        boolean useCompactFormat = true;
        int usersPerPage = Constants.DEFAULT_CONVERSIONS_PER_PAGE;
        String messageText = userMessageBuilder.buildUsersMessage(users, 0, useCompactFormat, usersPerPage);
        var keyboard = paginationKeyboardBuilder.buildUsersPaginationKeyboard(users.size(), 0, usersPerPage, useCompactFormat);
        SendMessage sendMessage = new SendMessage(chatId, messageText).parseMode(ParseMode.Markdown);
        if (keyboard != null) {
            sendMessage.replyMarkup(keyboard);
        }
        return sendMessage;
    }

    @Override
    public String getCommand() {
        return "/users";
    }

    @Override
    public String getDescription() {
        return "command.users.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "ADMIN".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }

    public UsersCallbackHandler getCallbackHandler() {
        return usersCallbackHandler;
    }
} 