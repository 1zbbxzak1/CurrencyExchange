package ru.julia.currencyexchange.application.bot.listener;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.executor.interfaces.Executor;
import ru.julia.currencyexchange.application.bot.messages.DefaultMessages;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class MessagesListener implements UpdatesListener {
    private final Executor executor;
    private final DefaultMessages defaultMessages;

    public MessagesListener(Executor executor, DefaultMessages defaultMessages) {
        this.executor = executor;
        this.defaultMessages = defaultMessages;
    }

    @Override
    public int process(List<Update> list) {
        list.forEach(update -> {
            if (update.callbackQuery() != null) {
                if (handleCallback(update)) {
                    return;
                }
            }

            SendMessage sendMessage = defaultMessages.sendMessage(update);
            if (sendMessage != null) {
                sendMessage.parseMode(ParseMode.Markdown);
                executor.execute(sendMessage);
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

        if (callbackData.startsWith("currency_to_rub_")) {
            return handleSimpleCallback(update,
                    () -> defaultMessages.getCurrencyToRubCallbackHandler().handleCallback(update));
        }

        if (callbackData.startsWith("convert_")) {
            return handleSimpleCallback(update,
                    () -> defaultMessages.getCurrencyConvertCallbackHandler().handleCallback(update));
        }

        return false;
    }

    private boolean handlePaginationCallback(Update update, String callbackData, String prefix,
                                             Function<Integer, EditMessageText> callbackHandler) {
        if (callbackData.startsWith(prefix)) {
            try {
                int page = Integer.parseInt(callbackData.substring(prefix.length()));
                EditMessageText editMessage = callbackHandler.apply(page);
                if (editMessage != null) {
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

    private boolean handleSimpleCallback(Update update, Supplier<EditMessageText> callbackHandler) {
        EditMessageText editMessage = callbackHandler.get();
        if (editMessage != null) {
            editMessage.parseMode(ParseMode.Markdown);
            executor.execute(editMessage);
        }
        return true;
    }
}
