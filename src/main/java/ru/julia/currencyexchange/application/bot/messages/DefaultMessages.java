package ru.julia.currencyexchange.application.bot.messages;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.infrastructure.bot.command.*;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyToRubCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyConvertCallbackHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultMessages {
    private final MessageConverter messageConverter;
    private final RegistrationStateService registrationStateService;

    private final List<BotCommandHandler> botCommands = new ArrayList<>();

    @Autowired
    private StartCommand startCommand;
    @Autowired
    private HelpCommand helpCommand;
    @Autowired
    private RegisterCommand registerCommand;
    @Autowired
    private CurrenciesCommand currenciesCommand;
    @Autowired
    private CurrencyToRubCommand currencyToRubCommand;
    @Autowired
    private CurrencyToRubCallbackHandler currencyToRubCallbackHandler;
    @Autowired
    private ConvertCommand convertCommand;
    @Autowired
    private CurrencyConvertCallbackHandler currencyConvertCallbackHandler;

    public DefaultMessages(MessageConverter messageConverter,
                           RegistrationStateService registrationStateService) {
        this.messageConverter = messageConverter;
        this.registrationStateService = registrationStateService;
    }

    @PostConstruct
    public void init() {
        addCommand(startCommand);
        addCommand(helpCommand);
        addCommand(registerCommand);
        addCommand(currenciesCommand);
        addCommand(currencyToRubCommand);
        addCommand(convertCommand);
    }

    public void addCommand(BotCommandHandler command) {
        botCommands.add(command);
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
                return botCommand.handle(update);
            }
        }

        return new SendMessage(
                chatId,
                messageConverter.resolve(
                        "message.unknown_command", Map.of("command", message != null ? message : "None")));
    }
}
