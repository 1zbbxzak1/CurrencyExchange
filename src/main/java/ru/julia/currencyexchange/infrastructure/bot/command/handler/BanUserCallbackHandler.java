package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

import java.util.Map;

@Component
public class BanUserCallbackHandler {
    private final MessageConverter messageConverter;
    private final UserService userService;

    public BanUserCallbackHandler(MessageConverter messageConverter, UserService userService) {
        this.messageConverter = messageConverter;
        this.userService = userService;
    }

    public EditMessageText handleCallback(Update update) {
        if (update == null || update.callbackQuery() == null || update.callbackQuery().message() == null) {
            return createErrorResponse(null, null, "command.banUser.error");
        }

        Long chatId = update.callbackQuery().message().chat().id();
        Integer messageId = update.callbackQuery().message().messageId();
        String callbackData = update.callbackQuery().data();

        try {
            User adminUser = userService.findUserByChatId(chatId);
            if (adminUser == null || !"ADMIN".equals(getUserRole(adminUser))) {
                return createErrorResponse(chatId, messageId, "command.banUser.no_access");
            }

            if (!callbackData.startsWith("ban_user_")) {
                return createErrorResponse(chatId, messageId, "command.banUser.error");
            }

            String email = callbackData.substring("ban_user_".length());
            if (email.isEmpty()) {
                return createErrorResponse(chatId, messageId, "command.banUser.error");
            }

            User userToBan = userService.findUserByEmail(email);

            if (userToBan.getChatId().equals(chatId)) {
                return createErrorResponse(chatId, messageId, "command.banUser.cannot_ban_self");
            }

            if (userToBan.isBanned()) {
                return createErrorResponse(chatId, messageId, "command.banUser.already_banned");
            }

            userToBan.setBanned(true);
            userService.saveUser(userToBan);

            String successMessage = messageConverter.resolve("command.banUser.success", 
                    Map.of("email", email, "username", userToBan.getUsername()));

            return new EditMessageText(chatId, messageId, successMessage)
                    .parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            return createErrorResponse(chatId, messageId, "command.banUser.error");
        }
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