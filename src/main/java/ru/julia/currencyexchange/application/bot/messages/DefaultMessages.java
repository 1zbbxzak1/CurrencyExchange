package ru.julia.currencyexchange.application.bot.messages;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.infrastructure.bot.command.CurrenciesCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.HelpCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.RegisterCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.StartCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultMessages {
    private final MessageConverter messageConverter;
    private final UserService userService;
    private final AuthService authService;
    private final RegistrationStateService registrationStateService;

    private final List<BotCommandHandler> botCommands = new ArrayList<>();
    
    @Autowired
    private CurrenciesCommand currenciesCommand;

    public DefaultMessages(MessageConverter messageConverter,
                           UserService userService,
                           AuthService authService,
                           RegistrationStateService registrationStateService) {
        this.messageConverter = messageConverter;
        this.userService = userService;
        this.authService = authService;
        this.registrationStateService = registrationStateService;
    }

    @PostConstruct
    public void init() {
        addCommand(new StartCommand(messageConverter, userService));
        addCommand(new HelpCommand(messageConverter, userService));
        addCommand(new RegisterCommand(messageConverter, authService, userService, registrationStateService));
        addCommand(currenciesCommand);
    }

    public void addCommand(BotCommandHandler command) {
        botCommands.add(command);
    }

    public CurrenciesCommand getCurrenciesCommand() {
        return currenciesCommand;
    }

    public SendMessage sendMessage(Update update) {
        if (update.callbackQuery() != null) {
            String callbackData = update.callbackQuery().data();
            if (callbackData != null && callbackData.startsWith("currencies_page_")) {
                try {
                    int page = Integer.parseInt(callbackData.substring("currencies_page_".length()));
                    if (currenciesCommand != null) {
                        var editMessage = currenciesCommand.getCallbackHandler().handleCallback(update, page);
                        // Возвращаем null, так как EditMessageText обрабатывается отдельно
                        return null;
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные callback'и
                }
            }
        }

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
