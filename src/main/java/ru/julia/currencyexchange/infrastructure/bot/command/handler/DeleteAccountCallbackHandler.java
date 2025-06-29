package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

@Component
public class DeleteAccountCallbackHandler {
    private final UserService userService;
    private final MessageConverter messageConverter;
    private final DeleteAccountValidationService validationService;

    public DeleteAccountCallbackHandler(UserService userService, MessageConverter messageConverter,
                                        DeleteAccountValidationService validationService) {
        this.userService = userService;
        this.messageConverter = messageConverter;
        this.validationService = validationService;
    }

    public SendMessage handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.message().chat().id();
        String data = callbackQuery.data();

        ValidationResult validation = validationService.validateUserForDeletion(chatId);

        if (!validation.isValid()) {
            return validation.createErrorMessage(chatId);
        }

        if (Constants.CALLBACK_CONFIRM.equals(data)) {
            userService.softDeleteUserByChatId(chatId);
            return new SendMessage(chatId, messageConverter.resolve("command.deleteAccount.success"))
                    .parseMode(ParseMode.Markdown);
        } else if (Constants.CALLBACK_CANCEL.equals(data)) {
            return new SendMessage(chatId, messageConverter.resolve("command.deleteAccount.cancelled"))
                    .parseMode(ParseMode.Markdown);
        } else {
            return new SendMessage(chatId, messageConverter.resolve("command.deleteAccount.error"));
        }
    }
} 