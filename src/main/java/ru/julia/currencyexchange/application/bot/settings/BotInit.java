package ru.julia.currencyexchange.application.bot.settings;

import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.listener.MessagesListener;
import ru.julia.currencyexchange.application.service.bot.CommandRegistryService;

@Component
public class BotInit {
    private final TelegramBot bot;
    private final MessagesListener messagesListener;
    private final CommandRegistryService commandRegistryService;

    public BotInit(TelegramBot bot,
                   MessagesListener messagesListener,
                   CommandRegistryService commandRegistryService) {
        this.bot = bot;
        this.messagesListener = messagesListener;
        this.commandRegistryService = commandRegistryService;
    }

    @PostConstruct
    public void start() {
        commandRegistryService.init();

        bot.setUpdatesListener(messagesListener);
    }

    public void close() {
        bot.shutdown();
    }
}