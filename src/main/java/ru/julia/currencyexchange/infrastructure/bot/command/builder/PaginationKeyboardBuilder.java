package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;

@Component
public class PaginationKeyboardBuilder {
    private final MessageConverter messageConverter;

    public PaginationKeyboardBuilder(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildPaginationKeyboard(int totalCurrencies,
                                                        int currentPage,
                                                        int currenciesPerPage) {
        PaginationConfig config = new PaginationConfig(
                "command.currencies.pagination",
                "currencies_page_"
        );
        return buildPaginationKeyboard(totalCurrencies, currentPage, currenciesPerPage, config);
    }

    private InlineKeyboardMarkup buildPaginationKeyboard(int totalItems,
                                                         int currentPage,
                                                         int itemsPerPage,
                                                         PaginationConfig config) {
        int totalPages = (totalItems - 1) / itemsPerPage + 1;

        if (totalPages <= 1) {
            return null;
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = new InlineKeyboardButton[2];

        if (currentPage > 0) {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".previous"))
                    .callbackData(config.callbackPrefix + (currentPage - 1));
        } else {
            buttons[0] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".last"))
                    .callbackData(config.callbackPrefix + (totalPages - 1));
        }

        if (currentPage < totalPages - 1) {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".next"))
                    .callbackData(config.callbackPrefix + (currentPage + 1));
        } else {
            buttons[1] = new InlineKeyboardButton(messageConverter.resolve(config.messagePrefix + ".first"))
                    .callbackData(config.callbackPrefix + "0");
        }

        keyboard.addRow(buttons);
        return keyboard;
    }

    public InlineKeyboardMarkup buildHistoryPaginationKeyboard(int totalConversions,
                                                               int currentPage,
                                                               int conversionsPerPage) {
        PaginationConfig config = new PaginationConfig(
                "command.history.pagination",
                "history_page_"
        );
        return buildPaginationKeyboard(totalConversions, currentPage, conversionsPerPage, config);
    }

    public InlineKeyboardMarkup buildFindByDatePaginationKeyboard(int totalConversions,
                                                                  int currentPage,
                                                                  int conversionsPerPage,
                                                                  String dateStr) {
        String[] dateParts = dateStr.split("-");
        String callbackDateStr = dateParts[0] + "_" + dateParts[1] + "_" + dateParts[2];

        PaginationConfig config = new PaginationConfig(
                "command.findByDate.pagination",
                "findByDate_page_" + callbackDateStr + "_"
        );

        return buildPaginationKeyboard(totalConversions, currentPage, conversionsPerPage, config);
    }

    public InlineKeyboardMarkup buildUsersPaginationKeyboard(int totalUsers,
                                                            int currentPage,
                                                            int usersPerPage,
                                                            boolean useCompactFormat) {
        PaginationConfig config = new PaginationConfig(
                "command.users.pagination",
                "users_page_" + (useCompactFormat ? "compact_" : "full_")
        );
        InlineKeyboardMarkup keyboard = buildPaginationKeyboard(totalUsers, currentPage, usersPerPage, config);
        // Добавим кнопку переключения режима
        String switchModeText = useCompactFormat ? messageConverter.resolve("command.users.pagination.switch_full") : messageConverter.resolve("command.users.pagination.switch_compact");
        String switchModeCallback = useCompactFormat ? "users_switch_full_" + currentPage : "users_switch_compact_" + currentPage;
        InlineKeyboardButton switchButton = new InlineKeyboardButton(switchModeText).callbackData(switchModeCallback);
        if (keyboard == null) {
            keyboard = new InlineKeyboardMarkup();
        }
        keyboard.addRow(switchButton);
        return keyboard;
    }

    private record PaginationConfig(String messagePrefix, String callbackPrefix) {
    }
} 