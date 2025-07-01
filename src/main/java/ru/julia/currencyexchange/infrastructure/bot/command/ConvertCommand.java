package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyConvertKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ConvertCommand extends AbstractCommandHandler {
    private final CurrencyConvertService currencyConvertService;
    private final UserService userService;
    private final CurrencyConvertKeyboardBuilder keyboardBuilder;

    public ConvertCommand(MessageConverter messageConverter,
                          CurrencyConvertService currencyConvertService,
                          UserService userService,
                          CurrencyConvertKeyboardBuilder keyboardBuilder) {
        super(messageConverter);
        this.currencyConvertService = currencyConvertService;
        this.userService = userService;
        this.keyboardBuilder = keyboardBuilder;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();
        String text = update.message().text();

        try {
            if (!validateUser(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.convert.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            ConversionState currentState = currencyConvertService.getState(chatId);
            if (currentState == ConversionState.WAITING_AMOUNT) {
                return handleAmountInput(chatId, text);
            }

            String[] parts = text.trim().split("\\s+");

            return switch (parts.length) {
                case 4 -> handleFullConvert(chatId, parts[1].toUpperCase(), parts[2].toUpperCase(), parts[3]);
                case 3 -> handleCurrencySelection(chatId, parts[1].toUpperCase(), parts[2].toUpperCase());
                case 2 -> handleSingleCurrency(chatId, parts[1].toUpperCase());
                default -> showFromCurrencySelection(chatId);
            };

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.error"));
        }
    }

    @Override
    public boolean matches(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return false;
        }

        String text = update.message().text();

        if (text.equals("/convert")) {
            return true;
        }

        Long chatId = update.message().chat().id();
        return currencyConvertService.getState(chatId) != ConversionState.NONE;
    }

    @Override
    public String getCommand() {
        return "/convert";
    }

    @Override
    public String getDescription() {
        return "command.convert.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "USER".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }

    private SendMessage handleAmountInput(Long chatId, String amountText) {
        try {
            if (!currencyConvertService.isValidAmount(amountText.trim())) {
                return new SendMessage(chatId,
                        messageConverter.resolve("command.convert.invalid_amount"));
            }

            CurrencyConvertService.ConversionData data = currencyConvertService.getData(chatId);
            if (data == null) {
                currencyConvertService.clearData(chatId);
                return new SendMessage(chatId,
                        messageConverter.resolve("command.convert.errors.general"));
            }

            // Выполняем конвертацию с сохранением в историю
            BigDecimal amount = currencyConvertService.parseAmount(amountText.trim());
            CurrencyConversion conversion = currencyConvertService.convertCurrency(
                    chatId,
                    data.fromCurrency(),
                    data.toCurrency(),
                    amount
            );

            String messageText = currencyConvertService.buildConversionMessage(conversion);

            SendMessage response = new SendMessage(chatId, messageText)
                    .parseMode(ParseMode.Markdown)
                    .replyMarkup(keyboardBuilder.buildBackKeyboard());

            currencyConvertService.clearData(chatId);

            return response;

        } catch (Exception e) {
            currencyConvertService.clearData(chatId);
            return new SendMessage(chatId,
                    messageConverter.resolve("command.convert.errors.conversion_failed"));
        }
    }

    private boolean validateUser(Long chatId) {
        if (!userService.existsByChatId(chatId)) {
            return false;
        }
        User user = userService.findUserByChatId(chatId);
        return !user.isBanned() && !user.isDeleted() && user.isVerified();
    }

    private SendMessage handleFullConvert(Long chatId, String fromCurrency, String toCurrency, String amountStr) {
        SendMessage validationError = validateCurrencies(chatId, fromCurrency, toCurrency);
        if (validationError != null) {
            return validationError;
        }

        if (!currencyConvertService.isValidAmount(amountStr)) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.invalid_amount"));
        }

        try {
            BigDecimal amount = currencyConvertService.parseAmount(amountStr);
            CurrencyConversion conversion = currencyConvertService.convertCurrency(chatId, fromCurrency, toCurrency, amount);
            String messageText = currencyConvertService.buildConversionMessage(conversion);

            return createMessageWithKeyboard(chatId, messageText, keyboardBuilder.buildBackKeyboard());

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.conversion_error"));
        }
    }

    private SendMessage handleCurrencySelection(Long chatId, String fromCurrency, String toCurrency) {
        SendMessage validationError = validateCurrencies(chatId, fromCurrency, toCurrency);
        if (validationError != null) {
            return validationError;
        }

        var keyboard = keyboardBuilder.buildAmountInputKeyboard(fromCurrency, toCurrency);
        String messageText = messageConverter.resolve("command.convert.conversion.title",
                Map.of("from", fromCurrency, "to", toCurrency)) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.selection.amount_selection");

        return createMessageWithKeyboard(chatId, messageText, keyboard);
    }

    private SendMessage handleSingleCurrency(Long chatId, String fromCurrency) {
        if (isValidCurrency(fromCurrency)) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.from_not_found",
                    Map.of("currency_code", fromCurrency)));
        }

        List<Currency> popularCurrencies = currencyConvertService.getPopularCurrencies();
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildToCurrencyKeyboard(popularCurrencies, fromCurrency);
        String messageText = messageConverter.resolve("command.convert.conversion.title_with_question",
                Map.of("from", fromCurrency)) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.selection.to_currency") + ":";

        return createMessageWithKeyboard(chatId, messageText, keyboard);
    }

    private SendMessage showFromCurrencySelection(Long chatId) {
        if (!currencyConvertService.hasCurrencies()) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.no_currencies"));
        }

        List<Currency> popularCurrencies = currencyConvertService.getPopularCurrencies();
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildFromCurrencyKeyboard(popularCurrencies);
        String messageText = messageConverter.resolve("command.convert.selection.title") +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.selection.from_currency") + ":";

        return createMessageWithKeyboard(chatId, messageText, keyboard);
    }

    private SendMessage validateCurrencies(Long chatId, String fromCurrency, String toCurrency) {
        if (isValidCurrency(fromCurrency)) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.from_not_found",
                    Map.of("currency_code", fromCurrency)));
        }

        if (isValidCurrency(toCurrency)) {
            return new SendMessage(chatId, messageConverter.resolve("command.convert.to_not_found",
                    Map.of("currency_code", toCurrency)));
        }

        return null;
    }

    private boolean isValidCurrency(String currencyCode) {
        return currencyConvertService.getCurrencyByCode(currencyCode) == null;
    }

    private SendMessage createMessageWithKeyboard(Long chatId, String messageText, InlineKeyboardMarkup keyboard) {
        return new SendMessage(chatId, messageText)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }
} 