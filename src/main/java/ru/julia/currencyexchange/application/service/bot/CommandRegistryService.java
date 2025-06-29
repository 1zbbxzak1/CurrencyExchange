package ru.julia.currencyexchange.application.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.List;

@Service
public class CommandRegistryService {
    private final TelegramBot bot;

    private final List<BotCommandHandler> commands;

    @Autowired
    public CommandRegistryService(TelegramBot bot, List<BotCommandHandler> commands) {
        this.bot = bot;
        this.commands = commands;
    }

    @PostConstruct
    public void init() {
        BotCommand[] botCommands =
                commands.stream().map(BotCommandHandler::toBotCommand).toArray(BotCommand[]::new);

        bot.execute(new SetMyCommands(botCommands));
    }
}
