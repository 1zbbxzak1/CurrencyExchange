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
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class FindByDateCommand extends AbstractCommandHandler {
    private final CurrencyExchangeService currencyExchangeService;
    private final UserService userService;
    private final HistoryMessageBuilder historyMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;

    public FindByDateCommand(MessageConverter messageConverter,
                             CurrencyExchangeService currencyExchangeService,
                             UserService userService,
                             HistoryMessageBuilder historyMessageBuilder,
                             PaginationKeyboardBuilder paginationKeyboardBuilder) {
        super(messageConverter);
        this.currencyExchangeService = currencyExchangeService;
        this.userService = userService;
        this.historyMessageBuilder = historyMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String username = update.message().chat().username();
        String text = update.message().text();

        try {
            if (!userService.existsByChatId(chatId)) {
                return new SendMessage(chatId, messageConverter.resolve("command.findByDate.error"));
            }

            User user = userService.findUserByChatId(chatId);
            if (user.isBanned() || user.isDeleted() || !user.isVerified()) {
                return new SendMessage(chatId, messageConverter.resolve("command.findByDate.error"));
            }

            userService.updateUsernameIfChanged(chatId, username);

            String[] parts = text.trim().split("\\s+");

            if (parts.length != 2) {
                return new SendMessage(chatId, messageConverter.resolve("command.findByDate.usage"));
            }

            String dateStr = parts[1];
            LocalDate date = parseDate(dateStr);

            List<CurrencyConversion> conversions = currencyExchangeService.findByCurrencyDate(user.getId(), dateStr);

            if (conversions.isEmpty()) {
                return new SendMessage(chatId, messageConverter.resolve("command.findByDate.no_conversions",
                        java.util.Map.of("date", date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))));
            }

            boolean useCompactFormat = conversions.size() > 20;
            int conversionsPerPage = useCompactFormat ? Constants.COMPACT_CONVERSIONS_PER_PAGE : Constants.DEFAULT_CONVERSIONS_PER_PAGE;

            String messageText = historyMessageBuilder.buildFindByDateMessage(conversions, 0, useCompactFormat, conversionsPerPage, dateStr);

            var keyboard = paginationKeyboardBuilder.buildFindByDatePaginationKeyboard(conversions.size(), 0, conversionsPerPage, dateStr);

            SendMessage sendMessage = new SendMessage(chatId, messageText)
                    .parseMode(ParseMode.Markdown);

            if (keyboard != null) {
                sendMessage.replyMarkup(keyboard);
            }

            return sendMessage;

        } catch (DateTimeParseException e) {
            return new SendMessage(chatId, messageConverter.resolve("command.findByDate.invalid_date"));
        } catch (Exception e) {
            return new SendMessage(chatId, messageConverter.resolve("command.findByDate.error"));
        }
    }

    private LocalDate parseDate(String dateStr) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return LocalDate.parse(dateStr, formatter);
    }

    @Override
    public String getCommand() {
        return "/findByDate";
    }

    @Override
    public String getDescription() {
        return "command.findByDate.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "USER".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }
} 