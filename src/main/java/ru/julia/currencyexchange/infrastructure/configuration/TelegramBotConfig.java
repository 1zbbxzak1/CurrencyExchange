package ru.julia.currencyexchange.infrastructure.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TelegramBotConfig {
    @Bean
    @Profile("!test")
    public TelegramBot telegramBot(BotConfig config) {
        return new TelegramBot(config.telegramToken());
    }
}