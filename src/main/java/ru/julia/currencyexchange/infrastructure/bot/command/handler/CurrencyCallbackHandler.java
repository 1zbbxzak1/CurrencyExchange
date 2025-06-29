package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;

@Component
public class CurrencyCallbackHandler {
    private final MessageConverter messageConverter;
    private final UserService userService;
    private final CurrencyExchangeService currencyExchangeService;
    private final CurrencyMessageBuilder currencyMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;

    public CurrencyCallbackHandler(MessageConverter messageConverter,
                                   UserService userService,
                                   CurrencyExchangeService currencyExchangeService,
                                   CurrencyMessageBuilder currencyMessageBuilder,
                                   PaginationKeyboardBuilder paginationKeyboardBuilder) {
        this.messageConverter = messageConverter;
        this.userService = userService;
        this.currencyExchangeService = currencyExchangeService;
        this.currencyMessageBuilder = currencyMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
    }

    public EditMessageText handleCallback(Update update, int page) {
        Long chatId = update.callbackQuery().message().chat().id();
        Integer messageId = update.callbackQuery().message().messageId();

        try {
            if (!userService.existsByChatId(chatId)) {
                return new EditMessageText(chatId, messageId, messageConverter.resolve("command.currencies.error"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned()) {
                return new EditMessageText(chatId, messageId, messageConverter.resolve("command.currencies.error"));
            }

            List<Currency> currencies = currencyExchangeService.getAllCurrencies();

            if (currencies.isEmpty()) {
                return new EditMessageText(chatId, messageId, messageConverter.resolve("command.currencies.no_currencies"));
            }

            boolean useCompactFormat = currencies.size() > 50;
            int currenciesPerPage = useCompactFormat ? Constants.CURRENCIES_PER_PAGE_COMPACT : Constants.CURRENCIES_PER_PAGE;

            int maxPage = (currencies.size() - 1) / currenciesPerPage;
            if (page < 0) page = 0;
            if (page > maxPage) page = maxPage;

            String messageText = currencyMessageBuilder.buildCurrenciesMessage(currencies, page, useCompactFormat, currenciesPerPage);

            var keyboard = paginationKeyboardBuilder.buildPaginationKeyboard(currencies.size(), page, currenciesPerPage);

            return new EditMessageText(chatId, messageId, messageText)
                    .parseMode(ParseMode.Markdown)
                    .replyMarkup(keyboard);

        } catch (Exception e) {
            return new EditMessageText(chatId, messageId, messageConverter.resolve("command.currencies.error"));
        }
    }
} 