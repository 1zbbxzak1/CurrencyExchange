package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.domain.model.User;

import java.util.List;

@Component
public class BanUserKeyboardBuilder {
    private final MessageConverter messageConverter;

    public BanUserKeyboardBuilder(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildEmailKeyboard(List<User> users) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        for (User user : users) {
            if (user.isBanned()) {
                continue;
            }

            String buttonText = user.getEmail();
            if (user.isDeleted()) {
                buttonText += " (удален)";
            } else if (!user.isVerified()) {
                buttonText += " (не верифицирован)";
            }

            InlineKeyboardButton button = new InlineKeyboardButton(buttonText)
                    .callbackData("ban_user_" + user.getEmail());
            keyboardMarkup.addRow(button);
        }

        return keyboardMarkup;
    }
} 