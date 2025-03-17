package ru.julia.currencyexchange.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.julia.currencyexchange.dto.CurrencyConversion;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseConfig {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<CurrencyConversion> conversionHistory() {
        return new ArrayList<>();
    }
}
