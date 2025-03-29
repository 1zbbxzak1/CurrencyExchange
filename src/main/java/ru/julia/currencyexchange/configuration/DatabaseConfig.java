package ru.julia.currencyexchange.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.julia.currencyexchange.dto.CurrencyConversion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class DatabaseConfig {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public Map<Long, CurrencyConversion> conversionHistory() {
        return new ConcurrentHashMap<Long, CurrencyConversion>();
    }
}
