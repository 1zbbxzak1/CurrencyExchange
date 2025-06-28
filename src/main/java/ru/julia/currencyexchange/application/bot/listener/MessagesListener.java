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
                String callbackData = update.callbackQuery().data();
                if (callbackData != null && callbackData.startsWith("currencies_page_")) {
                    try {
                        int page = Integer.parseInt(callbackData.substring("currencies_page_".length()));
                        var editMessage = defaultMessages.getCurrenciesCommand().getCallbackHandler().handleCallback(update, page);
                        if (editMessage != null) {
                            editMessage.parseMode(ParseMode.Markdown);
                            executor.execute(editMessage);
                        }
                        return;
                    } catch (NumberFormatException e) {
                        // Игнорируем некорректные callback'и
                    }
                }
                
                if (callbackData != null && callbackData.startsWith("currency_to_rub_")) {
                    EditMessageText editMessage = defaultMessages.getCurrencyToRubCallbackHandler().handleCallback(update);
                    if (editMessage != null) {
                        editMessage.parseMode(ParseMode.Markdown);
                        executor.execute(editMessage);
                    }
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
}
