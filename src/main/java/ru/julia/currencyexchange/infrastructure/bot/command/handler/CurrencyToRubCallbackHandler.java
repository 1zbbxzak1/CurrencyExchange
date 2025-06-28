package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyToRubKeyboardBuilder;

import java.util.List;

@Component
public class CurrencyToRubCallbackHandler {
    private final CurrencyToRubService currencyToRubService;
    private final CurrencyToRubKeyboardBuilder keyboardBuilder;

    public CurrencyToRubCallbackHandler(CurrencyToRubService currencyToRubService,
                                        CurrencyToRubKeyboardBuilder keyboardBuilder) {
        this.currencyToRubService = currencyToRubService;
        this.keyboardBuilder = keyboardBuilder;
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
            return createErrorMessage(callbackQuery, "❌ Произошла ошибка. Попробуйте позже.");
        }
    }

    private EditMessageText showAllCurrencies(CallbackQuery callbackQuery) {
        List<Currency> allCurrencies = currencyToRubService.getAllCurrencies();
        var keyboard = keyboardBuilder.buildAllCurrenciesKeyboard(allCurrencies);

        String messageText = "💱 *Выберите валюту для просмотра курса к рублю:*\n\n" +
                "Все доступные валюты:";

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

        String messageText = "💱 *Выберите валюту для просмотра курса к рублю:*\n\n" +
                "Популярные валюты:";

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
                    "❌ Валюта с кодом '" + currencyCode + "' не найдена.");
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