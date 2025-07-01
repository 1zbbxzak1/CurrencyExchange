package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class FindByDateCallbackHandler {

    private final MessageConverter messageConverter;
    private final UserService userService;
    private final CurrencyExchangeService currencyExchangeService;
    private final HistoryMessageBuilder historyMessageBuilder;
    private final PaginationKeyboardBuilder paginationKeyboardBuilder;

    public FindByDateCallbackHandler(MessageConverter messageConverter,
                                     UserService userService,
                                     CurrencyExchangeService currencyExchangeService,
                                     HistoryMessageBuilder historyMessageBuilder,
                                     PaginationKeyboardBuilder paginationKeyboardBuilder) {
        this.messageConverter = messageConverter;
        this.userService = userService;
        this.currencyExchangeService = currencyExchangeService;
        this.historyMessageBuilder = historyMessageBuilder;
        this.paginationKeyboardBuilder = paginationKeyboardBuilder;
    }

    public EditMessageText handleCallback(Update update, int page, String dateStr) {
        if (update == null || update.callbackQuery() == null || update.callbackQuery().message() == null) {
            return createErrorResponse(null, null, "command.findByDate.error");
        }

        Long chatId = update.callbackQuery().message().chat().id();
        Integer messageId = update.callbackQuery().message().messageId();

        try {
            User user = validateAndGetUser(chatId);
            if (user == null) {
                return createErrorResponse(chatId, messageId, "command.findByDate.error");
            }

            List<CurrencyConversion> conversions = currencyExchangeService.findByCurrencyDate(user.getId(), dateStr);

            if (conversions.isEmpty()) {
                LocalDate date = LocalDate.parse(dateStr);
                return createErrorResponse(chatId, messageId, "command.findByDate.no_conversions", 
                    java.util.Map.of("date", date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }

            PaginationParams paginationParams = calculatePaginationParams(conversions.size(), page);

            return buildFindByDateResponse(chatId, messageId, conversions, paginationParams, dateStr);

        } catch (Exception e) {
            return createErrorResponse(chatId, messageId, "command.findByDate.error");
        }
    }

    private EditMessageText createErrorResponse(Long chatId, Integer messageId, String errorMessageKey) {
        return createErrorResponse(chatId, messageId, errorMessageKey, java.util.Map.of());
    }

    private EditMessageText createErrorResponse(Long chatId, Integer messageId, String errorMessageKey, java.util.Map<String, String> params) {
        String errorMessage = messageConverter.resolve(errorMessageKey, params);
        return new EditMessageText(chatId, messageId, errorMessage);
    }

    private User validateAndGetUser(Long chatId) {
        if (!userService.existsByChatId(chatId)) {
            return null;
        }

        User user = userService.findUserByChatId(chatId);
        if (user.isBanned()) {
            return null;
        }

        return user;
    }

    private PaginationParams calculatePaginationParams(int totalConversions, int requestedPage) {
        boolean useCompactFormat = totalConversions > Constants.COMPACT_FORMAT_THRESHOLD;
        int conversionsPerPage = useCompactFormat ? Constants.COMPACT_CONVERSIONS_PER_PAGE : Constants.DEFAULT_CONVERSIONS_PER_PAGE;

        int maxPage = Math.max(0, (totalConversions - 1) / conversionsPerPage);
        int validatedPage = Math.max(0, Math.min(requestedPage, maxPage));

        return new PaginationParams(validatedPage, conversionsPerPage, useCompactFormat, maxPage);
    }

    private EditMessageText buildFindByDateResponse(Long chatId, Integer messageId,
                                                    List<CurrencyConversion> conversions,
                                                    PaginationParams paginationParams,
                                                    String dateStr) {
        String messageText = historyMessageBuilder.buildFindByDateMessage(
                conversions,
                paginationParams.currentPage,
                paginationParams.useCompactFormat,
                paginationParams.conversionsPerPage,
                dateStr
        );

        var keyboard = paginationKeyboardBuilder.buildFindByDatePaginationKeyboard(
                conversions.size(),
                paginationParams.currentPage,
                paginationParams.conversionsPerPage,
                dateStr
        );

        EditMessageText editMessage = new EditMessageText(chatId, messageId, messageText)
                .parseMode(ParseMode.Markdown);
        
        if (keyboard != null) {
            editMessage.replyMarkup(keyboard);
        }
        
        return editMessage;
    }

    private record PaginationParams(int currentPage, int conversionsPerPage, boolean useCompactFormat, int maxPage) {
    }
} 