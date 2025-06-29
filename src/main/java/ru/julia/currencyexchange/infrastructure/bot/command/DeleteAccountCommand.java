package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.DeleteAccountKeyboardBuilder;

@Component
public class DeleteAccountCommand extends AbstractCommandHandler {
    private final UserService userService;
    private final DeleteAccountKeyboardBuilder deleteAccountKeyboardBuilder;
    private final DeleteAccountValidationService validationService;

    public DeleteAccountCommand(MessageConverter messageConverter, UserService userService,
                                DeleteAccountKeyboardBuilder deleteAccountKeyboardBuilder,
                                DeleteAccountValidationService validationService) {
        super(messageConverter);
        this.userService = userService;
        this.deleteAccountKeyboardBuilder = deleteAccountKeyboardBuilder;
        this.validationService = validationService;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();

        ValidationResult validation = validationService.validateUserForDeletion(chatId);

        if (!validation.isValid()) {
            return validation.createErrorMessage(chatId);
        }

        InlineKeyboardMarkup keyboard = deleteAccountKeyboardBuilder.buildDeleteAccountKeyboard();
        return new SendMessage(chatId, messageConverter.resolve("command.deleteAccount.confirmation"))
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    @Override
    public String getCommand() {
        return "/deleteAccount";
    }

    @Override
    public String getDescription() {
        return "command.deleteAccount.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && user.isVerified() && !user.isBanned() && !user.isDeleted();
    }
} 