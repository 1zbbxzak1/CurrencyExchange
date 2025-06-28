package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyConvertKeyboardBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class CurrencyConvertCallbackHandler {
    private static final String CONVERT_PREFIX = "convert_";
    private static final String CURRENCY_PREFIX = "currency_";
    private static final String FROM_SHOW_ALL = "from_show_all";
    private static final String TO_SHOW_ALL_PREFIX = "to_show_all_";
    private static final String FROM_SHOW_POPULAR = "from_show_popular";
    private static final String TO_SHOW_POPULAR_PREFIX = "to_show_popular_";
    private static final String AMOUNT_PREFIX = "amount_";
    private static final String MANUAL_AMOUNT_PREFIX = "manual_amount_";
    private static final String BACK_TO_CURRENCY_SELECTION = "back_to_currency_selection";
    private static final String BACK_TO_SELECTION = "back_to_selection";
    private static final String FALLBACK_CURRENCY = "USD";
    
    private final CurrencyConvertService currencyConvertService;
    private final CurrencyConvertKeyboardBuilder keyboardBuilder;
    private final MessageConverter messageConverter;

    public CurrencyConvertCallbackHandler(CurrencyConvertService currencyConvertService,
                                         CurrencyConvertKeyboardBuilder keyboardBuilder,
                                         MessageConverter messageConverter) {
        this.currencyConvertService = currencyConvertService;
        this.keyboardBuilder = keyboardBuilder;
        this.messageConverter = messageConverter;
    }

    public EditMessageText handleCallback(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        String callbackData = callbackQuery.data();

        if (!isValidConvertCallback(callbackData)) {
            return null;
        }

        String action = callbackData.substring(CONVERT_PREFIX.length());

        try {
            return processCallbackAction(callbackQuery, action);
        } catch (Exception e) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.general"));
        }
    }

    private boolean isValidConvertCallback(String callbackData) {
        return callbackData != null && callbackData.startsWith(CONVERT_PREFIX);
    }

    private EditMessageText processCallbackAction(CallbackQuery callbackQuery, String action) {
        if (action.startsWith(CURRENCY_PREFIX)) {
            return handleCurrencySelection(callbackQuery, action.substring(CURRENCY_PREFIX.length()));
        } else if (action.equals(FROM_SHOW_ALL)) {
            return showAllFromCurrencies(callbackQuery);
        } else if (action.startsWith(TO_SHOW_ALL_PREFIX)) {
            String fromCurrency = action.substring(TO_SHOW_ALL_PREFIX.length());
            return showAllToCurrencies(callbackQuery, fromCurrency);
        } else if (action.equals(FROM_SHOW_POPULAR)) {
            return showPopularFromCurrencies(callbackQuery);
        } else if (action.startsWith(TO_SHOW_POPULAR_PREFIX)) {
            String fromCurrency = action.substring(TO_SHOW_POPULAR_PREFIX.length());
            return showPopularToCurrencies(callbackQuery, fromCurrency);
        } else if (action.startsWith(AMOUNT_PREFIX)) {
            return handleAmountSelection(callbackQuery, action.substring(AMOUNT_PREFIX.length()));
        } else if (action.startsWith(MANUAL_AMOUNT_PREFIX)) {
            return handleManualAmountInput(callbackQuery, action.substring(MANUAL_AMOUNT_PREFIX.length()));
        } else if (action.equals(BACK_TO_CURRENCY_SELECTION) || action.equals(BACK_TO_SELECTION)) {
            return showPopularFromCurrencies(callbackQuery);
        }

        return null;
    }

    private EditMessageText handleCurrencySelection(CallbackQuery callbackQuery, String currencyCode) {
        String currentText = callbackQuery.message().text();
        
        if (currentText.contains(messageConverter.resolve("command.convert.selection.from_currency"))) {
            return handleFromCurrencySelection(callbackQuery, currencyCode);
        } else if (currentText.contains(messageConverter.resolve("command.convert.selection.to_currency"))) {
            return handleToCurrencySelection(callbackQuery, currencyCode);
        }

        return null;
    }

    private EditMessageText handleFromCurrencySelection(CallbackQuery callbackQuery, String currencyCode) {
        List<Currency> popularCurrencies = currencyConvertService.getPopularCurrencies();
        var keyboard = keyboardBuilder.buildToCurrencyKeyboard(popularCurrencies, currencyCode);
        String messageText = messageConverter.resolve("command.convert.conversion.title_with_question",
                Map.of("from", currencyCode)) + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.to_currency") + ":";

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText handleToCurrencySelection(CallbackQuery callbackQuery, String currencyCode) {
        String fromCurrency = extractFromCurrency(callbackQuery.message().text());
        var keyboard = keyboardBuilder.buildAmountInputKeyboard(fromCurrency, currencyCode);
        String messageText = messageConverter.resolve("command.convert.conversion.title",
                Map.of("from", fromCurrency, "to", currencyCode)) + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.amount_selection");

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText showAllFromCurrencies(CallbackQuery callbackQuery) {
        List<Currency> allCurrencies = currencyConvertService.getAllCurrencies();
        var keyboard = keyboardBuilder.buildAllFromCurrenciesKeyboard(allCurrencies);
        String messageText = messageConverter.resolve("command.convert.selection.title") + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.from_currency") + ":";

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText showAllToCurrencies(CallbackQuery callbackQuery, String fromCurrency) {
        List<Currency> allCurrencies = currencyConvertService.getAllCurrencies();
        var keyboard = keyboardBuilder.buildAllToCurrenciesKeyboard(allCurrencies, fromCurrency);
        String messageText = messageConverter.resolve("command.convert.conversion.title_with_question",
                Map.of("from", fromCurrency)) + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.to_currency") + ":";

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText showPopularFromCurrencies(CallbackQuery callbackQuery) {
        List<Currency> popularCurrencies = currencyConvertService.getPopularCurrencies();
        var keyboard = keyboardBuilder.buildFromCurrencyKeyboard(popularCurrencies);
        String messageText = messageConverter.resolve("command.convert.selection.title") + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.from_currency") + ":";

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText showPopularToCurrencies(CallbackQuery callbackQuery, String fromCurrency) {
        List<Currency> popularCurrencies = currencyConvertService.getPopularCurrencies();
        var keyboard = keyboardBuilder.buildToCurrencyKeyboard(popularCurrencies, fromCurrency);
        String messageText = messageConverter.resolve("command.convert.conversion.title_with_question",
                Map.of("from", fromCurrency)) + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.to_currency") + ":";

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText handleAmountSelection(CallbackQuery callbackQuery, String amountData) {
        String[] parts = amountData.split("_");
        if (!isValidAmountData(parts)) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.invalid_format"));
        }

        String fromCurrency = parts[0];
        String toCurrency = parts[1];
        String amountStr = parts[2];

        try {
            return performCurrencyConversion(callbackQuery, fromCurrency, toCurrency, amountStr);
        } catch (Exception e) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.conversion_failed"));
        }
    }

    private boolean isValidAmountData(String[] parts) {
        return parts.length == 3;
    }

    private EditMessageText performCurrencyConversion(CallbackQuery callbackQuery, String fromCurrency, 
                                                     String toCurrency, String amountStr) {
        BigDecimal amount = currencyConvertService.parseAmount(amountStr);
        BigDecimal result = currencyConvertService.convertCurrency(fromCurrency, toCurrency, amount);
        String messageText = currencyConvertService.buildConversionMessage(fromCurrency, toCurrency, amount, result);
        var keyboard = keyboardBuilder.buildBackKeyboard();

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText handleManualAmountInput(CallbackQuery callbackQuery, String currencyData) {
        String[] parts = currencyData.split("_");
        if (parts.length != 2) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.invalid_format"));
        }

        String fromCurrency = parts[0];
        String toCurrency = parts[1];
        String messageText = messageConverter.resolve("command.convert.conversion.title",
                Map.of("from", fromCurrency, "to", toCurrency)) + 
                           "\n\n" + messageConverter.resolve("command.convert.selection.manual_amount_instruction");

        return createEditMessage(callbackQuery, messageText, null);
    }

    private String extractFromCurrency(String text) {
        int startIndex = text.indexOf(": ") + 2;
        int endIndex = text.indexOf(" →");
        if (startIndex > 1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex).trim();
        }
        return FALLBACK_CURRENCY;
    }

    private EditMessageText createEditMessage(CallbackQuery callbackQuery, String messageText, InlineKeyboardMarkup keyboard) {
        EditMessageText editMessage = new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                messageText
        ).parseMode(ParseMode.Markdown);
        
        if (keyboard != null) {
            editMessage.replyMarkup(keyboard);
        }
        
        return editMessage;
    }

    private EditMessageText createErrorMessage(CallbackQuery callbackQuery, String errorMessage) {
        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                errorMessage
        );
    }
} 