package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyCallbackHandler;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;

@Component
public class CurrenciesCommand extends AbstractCommandHandler {
    private final CurrencyExchangeService currencyExchangeService;
    private final UserService userService;
    private final CurrencyMessageBuilder currencyMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;
    private final CurrencyCallbackHandler currencyCallbackHandler;

    public CurrenciesCommand(MessageConverter messageConverter,
                             CurrencyExchangeService currencyExchangeService,
                             UserService userService,
                             CurrencyMessageBuilder currencyMessageBuilder,
                             PaginationKeyboardBuilder paginationKeyboardBuilder,
                             CurrencyCallbackHandler currencyCallbackHandler) {
        super(messageConverter);
        this.currencyExchangeService = currencyExchangeService;
        this.userService = userService;
        this.currencyMessageBuilder = currencyMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
        this.currencyCallbackHandler = currencyCallbackHandler;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();

        try {
            if (!userService.existsByChatId(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencies.error"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned()) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencies.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            List<Currency> currencies = currencyExchangeService.getAllCurrencies();

            if (currencies.isEmpty()) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencies.no_currencies"));
            }

            boolean useCompactFormat = currencies.size() > 50;
            int currenciesPerPage = useCompactFormat ? Constants.CURRENCIES_PER_PAGE_COMPACT : Constants.CURRENCIES_PER_PAGE;

            String messageText = currencyMessageBuilder.buildCurrenciesMessage(currencies, 0, useCompactFormat, currenciesPerPage);

            var keyboard = paginationKeyboardBuilder.buildPaginationKeyboard(currencies.size(), 0, currenciesPerPage);

            return new SendMessage(chatId, messageText)
                    .parseMode(ParseMode.Markdown)
                    .replyMarkup(keyboard);

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.currencies.error"));
        }
    }

    @Override
    public String getCommand() {
        return "/currencies";
    }

    @Override
    public String getDescription() {
        return "command.currencies.description";
    }

    public CurrencyCallbackHandler getCallbackHandler() {
        return currencyCallbackHandler;
    }
} 