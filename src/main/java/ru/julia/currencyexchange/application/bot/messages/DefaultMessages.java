package ru.julia.currencyexchange.application.bot.messages;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.CurrenciesCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.RegisterCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyConvertCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyToRubCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.FindByDateCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.HistoryCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.List;
import java.util.Map;

@Component
public class DefaultMessages {
    private final MessageConverter messageConverter;
    private final RegistrationStateService registrationStateService;
    private final UserService userService;
    private final List<BotCommandHandler> botCommands;

    private final CurrenciesCommand currenciesCommand;
    private final CurrencyToRubCallbackHandler currencyToRubCallbackHandler;
    private final CurrencyConvertCallbackHandler currencyConvertCallbackHandler;
    private final HistoryCallbackHandler historyCallbackHandler;
    private final FindByDateCallbackHandler findByDateCallbackHandler;

    public DefaultMessages(MessageConverter messageConverter,
                           RegistrationStateService registrationStateService,
                           UserService userService,
                           List<BotCommandHandler> botCommands,
                           CurrenciesCommand currenciesCommand,
                           CurrencyToRubCallbackHandler currencyToRubCallbackHandler,
                           CurrencyConvertCallbackHandler currencyConvertCallbackHandler,
                           HistoryCallbackHandler historyCallbackHandler,
                           FindByDateCallbackHandler findByDateCallbackHandler) {
        this.messageConverter = messageConverter;
        this.registrationStateService = registrationStateService;
        this.userService = userService;
        this.botCommands = botCommands;
        this.currenciesCommand = currenciesCommand;
        this.currencyToRubCallbackHandler = currencyToRubCallbackHandler;
        this.currencyConvertCallbackHandler = currencyConvertCallbackHandler;
        this.historyCallbackHandler = historyCallbackHandler;
        this.findByDateCallbackHandler = findByDateCallbackHandler;
    }

    public CurrenciesCommand getCurrenciesCommand() {
        return currenciesCommand;
    }

    public CurrencyToRubCallbackHandler getCurrencyToRubCallbackHandler() {
        return currencyToRubCallbackHandler;
    }

    public CurrencyConvertCallbackHandler getCurrencyConvertCallbackHandler() {
        return currencyConvertCallbackHandler;
    }

    public HistoryCallbackHandler getHistoryCallbackHandler() {
        return historyCallbackHandler;
    }

    public FindByDateCallbackHandler getFindByDateCallbackHandler() {
        return findByDateCallbackHandler;
    }

    public SendMessage sendMessage(Update update) {
        if (update.message() == null) {
            return null;
        }

        Long chatId = update.message().chat().id();
        String message = update.message().text();

        if (registrationStateService.getState(chatId) != RegistrationState.NONE) {
            for (BotCommandHandler botCommand : botCommands) {
                if (botCommand instanceof RegisterCommand) {
                    return botCommand.handle(update);
                }
            }
        }

        for (BotCommandHandler botCommand : botCommands) {
            if (botCommand.matches(update)) {
                if (!isCommandAccessible(botCommand, chatId)) {
                    String accessDeniedMessage = messageConverter.resolve("command.access_denied");
                    if (accessDeniedMessage == null || accessDeniedMessage.trim().isEmpty()) {
                        accessDeniedMessage = "❌ У вас нет доступа к этой команде.";
                    }
                    return new SendMessage(chatId, accessDeniedMessage);
                }
                return botCommand.handle(update);
            }
        }

        String unknownCommandMessage = messageConverter.resolve(
                "message.unknown_command", Map.of("command", message != null ? message : "None"));
        if (unknownCommandMessage == null || unknownCommandMessage.trim().isEmpty()) {
            unknownCommandMessage = "Неизвестная команда. Посмотрите список доступных команд, написав /help.";
        }
        return new SendMessage(chatId, unknownCommandMessage);
    }

    private boolean isCommandAccessible(BotCommandHandler command, Long chatId) {
        if (!userService.existsByChatId(chatId)) {
            return command.isAccessible(null);
        }

        User user = userService.findUserByChatId(chatId);
        if (user == null || user.isBanned()) {
            return false;
        }

        return command.isAccessible(user);
    }
}
