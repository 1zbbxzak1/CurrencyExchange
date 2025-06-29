package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyConvertKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class CurrencyConvertCallbackHandler {

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

        String action = callbackData.substring(Constants.CONVERT_PREFIX.length());

        try {
            return processCallbackAction(callbackQuery, action);
        } catch (Exception e) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.general"));
        }
    }

    private boolean isValidConvertCallback(String callbackData) {
        return callbackData != null && callbackData.startsWith(Constants.CONVERT_PREFIX);
    }

    private EditMessageText processCallbackAction(CallbackQuery callbackQuery, String action) {
        if (action.startsWith(Constants.CURRENCY_PREFIX)) {
            return handleCurrencySelection(callbackQuery, action.substring(Constants.CURRENCY_PREFIX.length()));
        } else if (action.equals(Constants.FROM_SHOW_ALL)) {
            return showCurrencySelection(callbackQuery, true, false, null);
        } else if (action.startsWith(Constants.TO_SHOW_ALL_PREFIX)) {
            String fromCurrency = action.substring(Constants.TO_SHOW_ALL_PREFIX.length());
            return showCurrencySelection(callbackQuery, false, false, fromCurrency);
        } else if (action.equals(Constants.FROM_SHOW_POPULAR)) {
            return showCurrencySelection(callbackQuery, true, true, null);
        } else if (action.startsWith(Constants.TO_SHOW_POPULAR_PREFIX)) {
            String fromCurrency = action.substring(Constants.TO_SHOW_POPULAR_PREFIX.length());
            return showCurrencySelection(callbackQuery, false, true, fromCurrency);
        } else if (action.startsWith(Constants.AMOUNT_PREFIX)) {
            return handleAmountSelection(callbackQuery, action.substring(Constants.AMOUNT_PREFIX.length()));
        } else if (action.startsWith(Constants.MANUAL_AMOUNT_PREFIX)) {
            return handleManualAmountInput(callbackQuery, action.substring(Constants.MANUAL_AMOUNT_PREFIX.length()));
        } else if (action.equals(Constants.BACK_TO_CURRENCY_SELECTION) || action.equals(Constants.BACK_TO_SELECTION)) {
            return showCurrencySelection(callbackQuery, true, true, null);
        }

        return null;
    }

    private EditMessageText showCurrencySelection(CallbackQuery callbackQuery, boolean isFromCurrency,
                                                  boolean showPopular, String fromCurrency) {
        List<Currency> currencies = showPopular ?
                currencyConvertService.getPopularCurrencies() :
                currencyConvertService.getAllCurrencies();

        InlineKeyboardMarkup keyboard = isFromCurrency ?
                (showPopular ? keyboardBuilder.buildFromCurrencyKeyboard(currencies) :
                        keyboardBuilder.buildAllFromCurrenciesKeyboard(currencies)) :
                (showPopular ? keyboardBuilder.buildToCurrencyKeyboard(currencies, fromCurrency) :
                        keyboardBuilder.buildAllToCurrenciesKeyboard(currencies, fromCurrency));

        String messageText = buildCurrencySelectionMessage(isFromCurrency, fromCurrency);

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private String buildCurrencySelectionMessage(boolean isFromCurrency, String fromCurrency) {
        if (isFromCurrency) {
            return messageConverter.resolve("command.convert.selection.title") +
                    Constants.LINE_SEPARATOR +
                    Constants.LINE_SEPARATOR +
                    messageConverter.resolve("command.convert.selection.from_currency") + ":";
        } else {
            return messageConverter.resolve("command.convert.conversion.title_with_question",
                    Map.of("from", fromCurrency)) +
                    Constants.LINE_SEPARATOR +
                    Constants.LINE_SEPARATOR +
                    messageConverter.resolve("command.convert.selection.to_currency") + ":";
        }
    }

    private EditMessageText createErrorMessage(CallbackQuery callbackQuery, String errorMessage) {
        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                errorMessage
        );
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

    private EditMessageText handleAmountSelection(CallbackQuery callbackQuery, String amountData) {
        String[] parts = amountData.split("_");
        if (!isValidAmountData(parts)) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.invalid_format"));
        }

        String fromCurrency = parts[0];
        String toCurrency = parts[1];
        String amountStr = parts[2];

        try {
            if (!currencyConvertService.isValidAmount(amountStr)) {
                return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.invalid_amount"));
            }

            return performCurrencyConversion(callbackQuery, fromCurrency, toCurrency, amountStr);

        } catch (Exception e) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.conversion_failed"));
        }
    }

    private EditMessageText handleManualAmountInput(CallbackQuery callbackQuery, String currencyData) {
        String[] parts = currencyData.split("_");
        if (parts.length != 2) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.convert.errors.invalid_format"));
        }

        String fromCurrency = parts[0];
        String toCurrency = parts[1];

        currencyConvertService.setState(callbackQuery.message().chat().id(), ConversionState.WAITING_AMOUNT);
        currencyConvertService.setData(callbackQuery.message().chat().id(), fromCurrency, toCurrency);

        String messageText = buildConversionTitleMessage(fromCurrency, toCurrency) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.selection.manual_amount_instruction");

        return createEditMessage(callbackQuery, messageText, null);
    }

    private EditMessageText handleFromCurrencySelection(CallbackQuery callbackQuery, String currencyCode) {
        return showCurrencySelection(callbackQuery, false, true, currencyCode);
    }

    private EditMessageText handleToCurrencySelection(CallbackQuery callbackQuery, String currencyCode) {
        String fromCurrency = extractFromCurrency(callbackQuery.message().text());
        var keyboard = keyboardBuilder.buildAmountInputKeyboard(fromCurrency, currencyCode);
        String messageText = buildConversionTitleMessage(fromCurrency, currencyCode) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.selection.amount_selection");

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private String buildConversionTitleMessage(String fromCurrency, String toCurrency) {
        return messageConverter.resolve("command.convert.conversion.title",
                Map.of("from", fromCurrency, "to", toCurrency));
    }

    private boolean isValidAmountData(String[] parts) {
        return parts.length == 3;
    }

    private EditMessageText performCurrencyConversion(CallbackQuery callbackQuery,
                                                      String fromCurrency,
                                                      String toCurrency,
                                                      String amountStr) {
        BigDecimal amount = currencyConvertService.parseAmount(amountStr);
        BigDecimal result = currencyConvertService.convertCurrency(fromCurrency, toCurrency, amount);
        String messageText = currencyConvertService.buildConversionMessage(fromCurrency, toCurrency, amount, result);
        var keyboard = keyboardBuilder.buildBackKeyboard();

        return createEditMessage(callbackQuery, messageText, keyboard);
    }

    private EditMessageText createEditMessage(CallbackQuery callbackQuery,
                                              String messageText,
                                              InlineKeyboardMarkup keyboard) {
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

    private String extractFromCurrency(String text) {
        int startIndex = text.indexOf(": ") + 2;
        int endIndex = text.indexOf(" →");
        if (startIndex > 1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex).trim();
        }
        return Constants.FALLBACK_CURRENCY;
    }
} 