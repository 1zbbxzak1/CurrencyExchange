package ru.julia.currencyexchange.infrastructure.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrencyExchangeConfig {
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @PostConstruct
    public void init() {
        System.out.println(
                "Приложение: " + appName + System.lineSeparator() +
                        "Версия: " + appVersion);
    }
}
