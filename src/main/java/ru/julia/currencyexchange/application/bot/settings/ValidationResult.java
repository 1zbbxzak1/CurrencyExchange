package ru.julia.currencyexchange.application.bot.settings;

import com.pengrad.telegrambot.request.SendMessage;
import ru.julia.currencyexchange.domain.model.User;

public class ValidationResult {
    private final boolean isValid;
    private final User user;
    private final String errorMessage;

    private ValidationResult(boolean isValid, User user, String errorMessage) {
        this.isValid = isValid;
        this.user = user;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult success(User user) {
        return new ValidationResult(true, user, null);
    }

    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, null, errorMessage);
    }

    public boolean isValid() {
        return isValid;
    }

    public User getUser() {
        return user;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SendMessage createErrorMessage(Long chatId) {
        return new SendMessage(chatId, errorMessage);
    }
}
