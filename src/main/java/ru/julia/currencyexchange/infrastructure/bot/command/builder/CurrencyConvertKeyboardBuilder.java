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
public class CurrencyConvertKeyboardBuilder {
    private static final int POPULAR_BUTTONS_PER_ROW = 4;
    private static final int ALL_BUTTONS_PER_ROW = 3;
    
    private static final String[] AMOUNT_VALUES_ROW1 = {"1", "10", "100", "1000"};
    private static final String[] AMOUNT_VALUES_ROW2 = {"0.1", "0.5", "5", "50"};
    
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final MessageConverter messageConverter;

    public CurrencyConvertKeyboardBuilder(CurrencyEmojiUtils currencyEmojiUtils,
                                         MessageConverter messageConverter) {
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildFromCurrencyKeyboard(List<Currency> popularCurrencies) {
        return buildCurrencyKeyboard(popularCurrencies, POPULAR_BUTTONS_PER_ROW,
                messageConverter.resolve("command.convert.keyboard.show_all"), "convert_from_show_all");
    }

    public InlineKeyboardMarkup buildToCurrencyKeyboard(List<Currency> popularCurrencies, String selectedFromCurrency) {
        return buildCurrencyKeyboard(popularCurrencies, POPULAR_BUTTONS_PER_ROW,
                messageConverter.resolve("command.convert.keyboard.show_all"), "convert_to_show_all_" + selectedFromCurrency);
    }

    public InlineKeyboardMarkup buildAllFromCurrenciesKeyboard(List<Currency> allCurrencies) {
        return buildCurrencyKeyboard(allCurrencies, ALL_BUTTONS_PER_ROW,
                messageConverter.resolve("command.convert.keyboard.back_to_popular"), "convert_from_show_popular");
    }

    public InlineKeyboardMarkup buildAllToCurrenciesKeyboard(List<Currency> allCurrencies, String selectedFromCurrency) {
        return buildCurrencyKeyboard(allCurrencies, ALL_BUTTONS_PER_ROW,
                messageConverter.resolve("command.convert.keyboard.back_to_popular"), "convert_to_show_popular_" + selectedFromCurrency);
    }

    public InlineKeyboardMarkup buildAmountInputKeyboard(String fromCurrency, String toCurrency) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        
        keyboardMarkup.addRow(createAmountButtons(AMOUNT_VALUES_ROW1, fromCurrency, toCurrency));
        keyboardMarkup.addRow(createAmountButtons(AMOUNT_VALUES_ROW2, fromCurrency, toCurrency));
        
        keyboardMarkup.addRow(new InlineKeyboardButton(messageConverter.resolve("command.convert.keyboard.manual_input"))
                .callbackData("convert_manual_amount_" + fromCurrency + "_" + toCurrency));
        
        keyboardMarkup.addRow(new InlineKeyboardButton(messageConverter.resolve("command.convert.keyboard.back_to_currency"))
                .callbackData("convert_back_to_currency_selection"));
        
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup buildBackKeyboard() {
        return new InlineKeyboardMarkup()
                .addRow(new InlineKeyboardButton(messageConverter.resolve("command.convert.keyboard.back_to_convert"))
                        .callbackData("convert_back_to_selection"));
    }

    private InlineKeyboardMarkup buildCurrencyKeyboard(List<Currency> currencies, int buttonsPerRow,
                                                       String bottomButtonText, String bottomButtonCallback) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        
        for (Currency currency : currencies) {
            row.add(createCurrencyButton(currency));
            
            if (row.size() == buttonsPerRow) {
                keyboardMarkup.addRow(row.toArray(new InlineKeyboardButton[0]));
                row.clear();
            }
        }
        
        if (!row.isEmpty()) {
            keyboardMarkup.addRow(row.toArray(new InlineKeyboardButton[0]));
        }
        
        keyboardMarkup.addRow(new InlineKeyboardButton(bottomButtonText).callbackData(bottomButtonCallback));
        
        return keyboardMarkup;
    }

    private InlineKeyboardButton createCurrencyButton(Currency currency) {
        return new InlineKeyboardButton(
                currencyEmojiUtils.getCurrencyEmoji(currency.getCode()) + " " + currency.getCode()
        ).callbackData("convert_currency_" + currency.getCode());
    }

    private InlineKeyboardButton[] createAmountButtons(String[] amounts, String fromCurrency, String toCurrency) {
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[amounts.length];
        for (int i = 0; i < amounts.length; i++) {
            buttons[i] = new InlineKeyboardButton(amounts[i])
                    .callbackData("convert_amount_" + fromCurrency + "_" + toCurrency + "_" + amounts[i]);
        }
        return buttons;
    }
} 