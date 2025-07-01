package ru.julia.currencyexchange.utils.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestTelegramBotConfig {
    @Bean
    public TelegramBot telegramBot() {
        return mock(TelegramBot.class);
    }
}
