package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class CurrencyToRubKeyboardBuilder {
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final MessageConverter messageConverter;

    public CurrencyToRubKeyboardBuilder(CurrencyEmojiUtils currencyEmojiUtils,
                                       MessageConverter messageConverter) {
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildPopularCurrenciesKeyboard(List<Currency> popularCurrencies) {
        return buildCurrencyKeyboard(popularCurrencies, 4, 
                messageConverter.resolve("command.currencyToRub.keyboard.show_all"), "currency_to_rub_show_all");
    }

    public InlineKeyboardMarkup buildAllCurrenciesKeyboard(List<Currency> allCurrencies) {
        return buildCurrencyKeyboard(allCurrencies, 3, 
                messageConverter.resolve("command.currencyToRub.keyboard.back_to_popular"), "currency_to_rub_show_popular");
    }

    public InlineKeyboardMarkup buildBackKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton backButton = new InlineKeyboardButton(messageConverter.resolve("command.currencyToRub.keyboard.back_to_selection"))
                .callbackData("currency_to_rub_back_to_selection");
        keyboardMarkup.addRow(backButton);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup buildCurrencyKeyboard(List<Currency> currencies, int buttonsPerRow, 
                                                      String bottomButtonText, String bottomButtonCallback) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Currency currency : currencies) {
            InlineKeyboardButton button = new InlineKeyboardButton(
                    currencyEmojiUtils.getCurrencyEmoji(currency.getCode()) + " " + currency.getCode()
            ).callbackData("currency_to_rub_" + currency.getCode());

            row.add(button);

            if (row.size() == buttonsPerRow) {
                keyboardMarkup.addRow(row.toArray(new InlineKeyboardButton[0]));
                row.clear();
            }
        }

        if (!row.isEmpty()) {
            keyboardMarkup.addRow(row.toArray(new InlineKeyboardButton[0]));
        }

        InlineKeyboardButton bottomButton = new InlineKeyboardButton(bottomButtonText)
                .callbackData(bottomButtonCallback);
        keyboardMarkup.addRow(bottomButton);

        return keyboardMarkup;
    }
} 