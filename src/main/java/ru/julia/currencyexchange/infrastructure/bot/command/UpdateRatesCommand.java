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
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class UpdateRatesCommand extends AbstractCommandHandler {
    private final CurrencyExchangeService currencyExchangeService;
    private final UserService userService;

    public UpdateRatesCommand(MessageConverter messageConverter,
                              CurrencyExchangeService currencyExchangeService,
                              UserService userService) {
        super(messageConverter);
        this.currencyExchangeService = currencyExchangeService;
        this.userService = userService;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();

        try {
            if (!validateUser(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.updateRates.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            User user = userService.findUserByChatId(chatId);

            List<Currency> updatedCurrencies = currencyExchangeService.updateCurrencyRates(user.getId());

            String successMessage = messageConverter.resolve("command.updateRates.success",
                    Map.of("date", LocalDateTime.now().format(Constants.DATE_FORMATTER),
                            "count", String.valueOf(updatedCurrencies.size())));

            return new SendMessage(chatId, successMessage)
                    .parseMode(ParseMode.Markdown);

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.updateRates.error"));
        }
    }

    private boolean validateUser(Long chatId) {
        if (!userService.existsByChatId(chatId)) {
            return false;
        }
        
        User user = userService.findUserByChatId(chatId);
        return user != null && "ADMIN".equals(getUserRole(user)) && !user.isDeleted() && !user.isBanned();
    }

    @Override
    public String getCommand() {
        return "/updateRates";
    }

    @Override
    public String getDescription() {
        return "command.updateRates.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "ADMIN".equals(getUserRole(user)) && !user.isDeleted() && !user.isBanned();
    }
} 