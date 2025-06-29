package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.UserMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;

@Component
public class UsersCallbackHandler {
    private final MessageConverter messageConverter;
    private final UserService userService;
    private final UserMessageBuilder userMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;

    public UsersCallbackHandler(MessageConverter messageConverter,
                               UserService userService,
                               UserMessageBuilder userMessageBuilder,
                               PaginationKeyboardBuilder paginationKeyboardBuilder) {
        this.messageConverter = messageConverter;
        this.userService = userService;
        this.userMessageBuilder = userMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
    }

    public EditMessageText handleCallback(Update update, int page, boolean useCompactFormat) {
        if (update == null || update.callbackQuery() == null || update.callbackQuery().message() == null) {
            return createErrorResponse(null, null, "command.users.error");
        }
        Long chatId = update.callbackQuery().message().chat().id();
        Integer messageId = update.callbackQuery().message().messageId();
        User user = userService.findUserByChatId(chatId);
        if (user == null || !"ADMIN".equals(getUserRole(user))) {
            return createErrorResponse(chatId, messageId, "command.users.no_access");
        }
        List<User> users = userService.findAllUsers(null);
        if (users.isEmpty()) {
            return createErrorResponse(chatId, messageId, "command.users.empty");
        }
        int usersPerPage = Constants.DEFAULT_CONVERSIONS_PER_PAGE;
        String messageText = userMessageBuilder.buildUsersMessage(users, page, useCompactFormat, usersPerPage);
        var keyboard = paginationKeyboardBuilder.buildUsersPaginationKeyboard(users.size(), page, usersPerPage, useCompactFormat);
        return new EditMessageText(chatId, messageId, messageText)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    private EditMessageText createErrorResponse(Long chatId, Integer messageId, String errorMessageKey) {
        String errorMessage = messageConverter.resolve(errorMessageKey);
        return new EditMessageText(chatId, messageId, errorMessage);
    }

    private String getUserRole(User user) {
        return user.getRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().replace("ROLE_", ""))
                .findFirst().orElse("USER");
    }
} 