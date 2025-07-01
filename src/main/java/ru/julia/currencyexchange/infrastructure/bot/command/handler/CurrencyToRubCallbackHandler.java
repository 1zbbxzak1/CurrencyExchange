package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyToRubKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;
import java.util.Map;

@Component
public class CurrencyToRubCallbackHandler {
    private final CurrencyToRubService currencyToRubService;
    private final CurrencyToRubKeyboardBuilder keyboardBuilder;
    private final MessageConverter messageConverter;

    public CurrencyToRubCallbackHandler(CurrencyToRubService currencyToRubService,
                                        CurrencyToRubKeyboardBuilder keyboardBuilder,
                                        MessageConverter messageConverter) {
        this.currencyToRubService = currencyToRubService;
        this.keyboardBuilder = keyboardBuilder;
        this.messageConverter = messageConverter;
    }

    public EditMessageText handleCallback(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        String callbackData = callbackQuery.data();

        if (callbackData == null || !callbackData.startsWith("currency_to_rub_")) {
            return null;
        }

        String action = callbackData.substring("currency_to_rub_".length());

        try {
            return switch (action) {
                case "show_all" -> showAllCurrencies(callbackQuery);
                case "show_popular", "back_to_selection" -> showPopularCurrencies(callbackQuery);
                default -> showCurrencyRate(callbackQuery, action);
            };
        } catch (Exception e) {
            return createErrorMessage(callbackQuery, messageConverter.resolve("command.currencyToRub.error"));
        }
    }

    private EditMessageText showAllCurrencies(CallbackQuery callbackQuery) {
        List<Currency> allCurrencies = currencyToRubService.getAllCurrencies();
        var keyboard = keyboardBuilder.buildAllCurrenciesKeyboard(allCurrencies);

        String messageText = messageConverter.resolve("command.currencyToRub.selection.title") +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.selection.all_subtitle");

        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                messageText
        ).parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    private EditMessageText showPopularCurrencies(CallbackQuery callbackQuery) {
        List<Currency> popularCurrencies = currencyToRubService.getPopularCurrencies();
        var keyboard = keyboardBuilder.buildPopularCurrenciesKeyboard(popularCurrencies);

        String messageText = messageConverter.resolve("command.currencyToRub.selection.title") +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.currencyToRub.selection.popular_subtitle");

        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                messageText
        ).parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    private EditMessageText showCurrencyRate(CallbackQuery callbackQuery, String currencyCode) {
        Currency currency = currencyToRubService.getCurrencyByCode(currencyCode);
        if (currency == null) {
            return createErrorMessage(callbackQuery,
                    messageConverter.resolve("command.currencyToRub.not_found",
                            Map.of("currency_code", currencyCode)));
        }

        String messageText = currencyToRubService.buildCurrencyToRubMessage(currency);
        var keyboard = keyboardBuilder.buildBackKeyboard();

        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                messageText
        ).parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    private EditMessageText createErrorMessage(CallbackQuery callbackQuery, String errorMessage) {
        return new EditMessageText(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                errorMessage
        );
    }
}