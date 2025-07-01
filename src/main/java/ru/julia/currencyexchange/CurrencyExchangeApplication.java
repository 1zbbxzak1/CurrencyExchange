package ru.julia.currencyexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.julia.currencyexchange.infrastructure.configuration.BotConfig;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class CurrencyExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyExchangeApplication.class, args);
    }

}
