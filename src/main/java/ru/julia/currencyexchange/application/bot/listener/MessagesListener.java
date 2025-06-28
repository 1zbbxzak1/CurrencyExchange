package ru.julia.currencyexchange.application.bot.listener;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
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
            SendMessage sendMessage = defaultMessages.sendMessage(update);
            if (sendMessage != null) {
                sendMessage.parseMode(ParseMode.Markdown);
            }

            executor.execute(sendMessage);
        });

        return CONFIRMED_UPDATES_ALL;
    }
}
