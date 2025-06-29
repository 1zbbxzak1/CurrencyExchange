package ru.julia.currencyexchange.application.bot.listener;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.executor.interfaces.Executor;
import ru.julia.currencyexchange.application.bot.messages.DefaultMessages;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class MessagesListener implements UpdatesListener {
    private final Executor executor;
    private final DefaultMessages defaultMessages;

    @Autowired
    public MessagesListener(Executor executor, DefaultMessages defaultMessages) {
        this.executor = executor;
        this.defaultMessages = defaultMessages;
    }

    @Override
    public int process(List<Update> list) {
        list.forEach(update -> {
            try {
                if (update.callbackQuery() != null) {
                    if (handleCallback(update)) {
                        return;
                    }
                }

                SendMessage sendMessage = defaultMessages.sendMessage(update);
                if (sendMessage != null && sendMessage.getParameters() != null) {
                    sendMessage.parseMode(ParseMode.Markdown);
                    executor.execute(sendMessage);
                }
            } catch (Exception e) {

            }
        });

        return CONFIRMED_UPDATES_ALL;
    }

    private boolean handleCallback(Update update) {
        String callbackData = update.callbackQuery().data();
        if (callbackData == null) {
            return false;
        }

        if (handlePaginationCallback(update, callbackData, "currencies_page_",
                page -> defaultMessages.getCurrenciesCommand().getCallbackHandler().handleCallback(update, page))) {
            return true;
        }

        if (handlePaginationCallback(update, callbackData, "history_page_",
                page -> defaultMessages.getHistoryCallbackHandler().handleCallback(update, page))) {
            return true;
        }

        if (handleFindByDatePaginationCallback(update, callbackData)) {
            return true;
        }

        if (handleUsersPaginationCallback(update, callbackData)) {
            return true;
        }

        if (handleUsersSwitchModeCallback(update, callbackData)) {
            return true;
        }

        if (callbackData.startsWith("currency_to_rub_")) {
            return handleSimpleCallback(update,
                    () -> defaultMessages.getCurrencyToRubCallbackHandler().handleCallback(update));
        }

        if (callbackData.startsWith("convert_")) {
            return handleSimpleCallback(update,
                    () -> defaultMessages.getCurrencyConvertCallbackHandler().handleCallback(update));
        }

        if (callbackData.equals(Constants.CALLBACK_CONFIRM) || callbackData.equals(Constants.CALLBACK_CANCEL)) {
            executor.execute(defaultMessages.getDeleteAccountCallbackHandler().handleCallback(update.callbackQuery()));
            return true;
        }

        return false;
    }

    private boolean handlePaginationCallback(Update update, String callbackData, String prefix,
                                             Function<Integer, EditMessageText> callbackHandler) {
        if (callbackData.startsWith(prefix)) {
            try {
                int page = Integer.parseInt(callbackData.substring(prefix.length()));
                EditMessageText editMessage = callbackHandler.apply(page);
                if (editMessage != null && editMessage.getParameters() != null) {
                    editMessage.parseMode(ParseMode.Markdown);
                    executor.execute(editMessage);
                }
                return true;
            } catch (NumberFormatException e) {
                // Игнорируем некорректные callback'и
            }
        }
        return false;
    }

    private boolean handleFindByDatePaginationCallback(Update update, String callbackData) {
        if (callbackData.startsWith("findByDate_page_")) {
            try {
                String[] parts = callbackData.substring("findByDate_page_".length()).split("_");
                if (parts.length >= 4) {
                    String dateStr = parts[0] + "-" + parts[1] + "-" + parts[2];
                    int page = Integer.parseInt(parts[3]);

                    EditMessageText editMessage = defaultMessages.getFindByDateCallbackHandler()
                            .handleCallback(update, page, dateStr);
                    if (editMessage != null && editMessage.getParameters() != null) {
                        editMessage.parseMode(ParseMode.Markdown);
                        executor.execute(editMessage);
                    }
                    return true;
                }
            } catch (NumberFormatException e) {
                // Игнорируем некорректные callback'и
            }
        }
        return false;
    }

    private boolean handleUsersPaginationCallback(Update update, String callbackData) {
        if (callbackData.startsWith("users_page_")) {
            try {
                String[] parts = callbackData.substring("users_page_".length()).split("_");
                if (parts.length >= 2) {
                    boolean useCompactFormat = "compact".equals(parts[0]);
                    int page = Integer.parseInt(parts[1]);

                    EditMessageText editMessage = defaultMessages.getUsersCallbackHandler()
                            .handleCallback(update, page, useCompactFormat);
                    if (editMessage != null && editMessage.getParameters() != null) {
                        editMessage.parseMode(ParseMode.Markdown);
                        executor.execute(editMessage);
                    }
                    return true;
                }
            } catch (NumberFormatException e) {
                // Игнорируем некорректные callback'и
            }
        }
        return false;
    }

    private boolean handleUsersSwitchModeCallback(Update update, String callbackData) {
        if (callbackData.startsWith("users_switch_")) {
            try {
                String[] parts = callbackData.substring("users_switch_".length()).split("_");
                if (parts.length >= 2) {
                    boolean useCompactFormat = "compact".equals(parts[0]);
                    int page = Integer.parseInt(parts[1]);

                    EditMessageText editMessage = defaultMessages.getUsersCallbackHandler()
                            .handleCallback(update, page, useCompactFormat);
                    if (editMessage != null && editMessage.getParameters() != null) {
                        editMessage.parseMode(ParseMode.Markdown);
                        executor.execute(editMessage);
                    }
                    return true;
                }
            } catch (NumberFormatException e) {
                // Игнорируем некорректные callback'и
            }
        }
        return false;
    }

    private boolean handleSimpleCallback(Update update, Supplier<EditMessageText> callbackHandler) {
        try {
            EditMessageText editMessage = callbackHandler.get();
            if (editMessage != null && editMessage.getParameters() != null) {
                editMessage.parseMode(ParseMode.Markdown);
                executor.execute(editMessage);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
