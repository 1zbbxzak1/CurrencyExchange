package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

@Service
public class DeleteAccountValidationService {
    private final UserService userService;
    private final MessageConverter messageConverter;

    public DeleteAccountValidationService(UserService userService, MessageConverter messageConverter) {
        this.userService = userService;
        this.messageConverter = messageConverter;
    }

    public ValidationResult validateUserForDeletion(Long chatId) {
        try {
            if (!userService.existsByChatId(chatId)) {
                return ValidationResult.error(messageConverter.resolve("command.deleteAccount.not_registered"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned()) {
                return ValidationResult.error(messageConverter.resolve("command.deleteAccount.banned"));
            }

            if (user.isDeleted()) {
                return ValidationResult.error(messageConverter.resolve("command.deleteAccount.already_deleted"));
            }

            return ValidationResult.success(user);
        } catch (Exception e) {
            return ValidationResult.error(messageConverter.resolve("command.deleteAccount.error"));
        }
    }
} 