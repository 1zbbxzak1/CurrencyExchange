package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.HistoryCallbackHandler;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;

@Component
public class HistoryCommand extends AbstractCommandHandler {
    private final CurrencyExchangeService currencyExchangeService;
    private final UserService userService;
    private final HistoryMessageBuilder historyMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;
    private final HistoryCallbackHandler historyCallbackHandler;

    public HistoryCommand(MessageConverter messageConverter,
                          CurrencyExchangeService currencyExchangeService,
                          UserService userService,
                          HistoryMessageBuilder historyMessageBuilder,
                          PaginationKeyboardBuilder paginationKeyboardBuilder,
                          HistoryCallbackHandler historyCallbackHandler) {
        super(messageConverter);
        this.currencyExchangeService = currencyExchangeService;
        this.userService = userService;
        this.historyMessageBuilder = historyMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
        this.historyCallbackHandler = historyCallbackHandler;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();

        try {
            if (!userService.existsByChatId(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.history.error"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned() || user.isDeleted() || !user.isVerified()) {
                return new SendMessage(chatId, messageConverter.resolve("command.history.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            List<CurrencyConversion> conversions = currencyExchangeService.getUserHistory(user.getId());

            if (conversions.isEmpty()) {
                return new SendMessage(chatId, messageConverter.resolve("command.history.no_conversions"));
            }

            boolean useCompactFormat = conversions.size() > 20;
            int conversionsPerPage = useCompactFormat ? Constants.COMPACT_CONVERSIONS_PER_PAGE : Constants.DEFAULT_CONVERSIONS_PER_PAGE;

            String messageText = historyMessageBuilder.buildHistoryMessage(conversions, 0, useCompactFormat, conversionsPerPage);

            var keyboard = paginationKeyboardBuilder.buildHistoryPaginationKeyboard(conversions.size(), 0, conversionsPerPage);

            SendMessage sendMessage = new SendMessage(chatId, messageText)
                    .parseMode(ParseMode.Markdown);

            if (keyboard != null) {
                sendMessage.replyMarkup(keyboard);
            }

            return sendMessage;

        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.history.error"));
        }
    }

    @Override
    public String getCommand() {
        return "/history";
    }

    @Override
    public String getDescription() {
        return "command.history.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "USER".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }

    public HistoryCallbackHandler getCallbackHandler() {
        return historyCallbackHandler;
    }
} 