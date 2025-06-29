package ru.julia.currencyexchange;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;

@SpringBootTest
@PostgresTestcontainers
public class CurrencyExchangeApplicationTests {
    @Test
    public void contextLoads() {
    }
}
