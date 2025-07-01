package ru.julia.currencyexchange.utils.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DatabaseCleanerConfig {
    @Bean
    public DatabaseCleaner databaseCleaner() {
        return new DatabaseCleaner();
    }
}
