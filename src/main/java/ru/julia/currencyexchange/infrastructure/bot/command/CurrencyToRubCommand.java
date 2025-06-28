package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyToRubKeyboardBuilder;

import java.util.List;
import java.util.Map;

@Component
public class CurrencyToRubCommand extends AbstractCommandHandler {
    private final CurrencyToRubService currencyToRubService;
    private final UserService userService;
    private final CurrencyToRubKeyboardBuilder keyboardBuilder;

    public CurrencyToRubCommand(MessageConverter messageConverter,
                                CurrencyToRubService currencyToRubService,
                                UserService userService,
                                CurrencyToRubKeyboardBuilder keyboardBuilder) {
        super(messageConverter);
        this.currencyToRubService = currencyToRubService;
        this.userService = userService;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();
        String text = update.message().text();

        try {
            if (!userService.existsByChatId(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencyToRub.error"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned()) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencyToRub.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            String[] parts = text.trim().split("\\s+");
            String currencyCode = null;

            if (parts.length > 1) {
                currencyCode = parts[1].toUpperCase();
            }

            if (currencyCode == null || currencyCode.isEmpty()) {
                return showCurrencySelectionKeyboard(chatId);
            }

            Currency currency = currencyToRubService.getCurrencyByCode(currencyCode);
            if (currency == null) {
                return new SendMessage(chatId, messageConverter.resolve("command.currencyToRub.not_found",
                        Map.of("currency_code", currencyCode)));
            }

            String messageText = currencyToRubService.buildCurrencyToRubMessage(currency);
            var keyboard = keyboardBuilder.buildBackKeyboard();

            return new SendMessage(chatId, messageText)
                    .parseMode(ParseMode.Markdown)
                    .replyMarkup(keyboard);

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.currencyToRub.error"));
        }
    }

    private SendMessage showCurrencySelectionKeyboard(Long chatId) {
        if (!currencyToRubService.hasCurrencies()) {
            return new SendMessage(chatId, messageConverter.resolve("command.currencyToRub.no_currencies"));
        }

        List<Currency> popularCurrencies = currencyToRubService.getPopularCurrencies();
        var keyboard = keyboardBuilder.buildPopularCurrenciesKeyboard(popularCurrencies);

        String messageText = messageConverter.resolve("command.currencyToRub.selection.title") + "\n\n" +
                messageConverter.resolve("command.currencyToRub.selection.popular_subtitle");

        return new SendMessage(chatId, messageText)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    @Override
    public String getCommand() {
        return "/currencyToRub";
    }

    @Override
    public String getDescription() {
        return "command.currencyToRub.description";
    }
}