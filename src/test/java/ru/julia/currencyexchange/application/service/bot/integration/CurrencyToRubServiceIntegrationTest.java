package ru.julia.currencyexchange.application.service.bot.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class CurrencyToRubServiceIntegrationTest {
    @Autowired
    private CurrencyToRubService service;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("getCurrencyByCode: найдено и не найдено")
    void getCurrencyByCode_foundAndNotFound() {
        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));

        assertThat(service.getCurrencyByCode("USD")).isNotNull();
        assertThat(service.getCurrencyByCode("XXX")).isNull();
    }

    @Test
    @DisplayName("hasCurrencies: true, false, null")
    void hasCurrencies_variants() {
        assertThat(service.hasCurrencies()).isFalse();

        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));

        assertThat(service.hasCurrencies()).isTrue();
    }

    @Test
    @DisplayName("getAllCurrencies: список и пусто")
    void getAllCurrencies_variants() {
        assertThat(service.getAllCurrencies()).isEmpty();

        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));

        assertThat(service.getAllCurrencies()).hasSize(1);
    }

    @Test
    @DisplayName("getPopularCurrencies: популярные, нет популярных, пусто")
    void getPopularCurrencies_variants() {
        assertThat(service.getPopularCurrencies()).isEmpty();

        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));
        currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.TEN));
        currencyRepository.save(new Currency("ABC", "Тест", BigDecimal.ONE));

        List<Currency> popular = service.getPopularCurrencies();
        assertThat(popular).extracting(Currency::getCode).contains("USD", "RUB").doesNotContain("ABC");

        currencyRepository.deleteAll();
        currencyRepository.save(new Currency("ZZZ", "NoPopular", BigDecimal.ONE));
        assertThat(service.getPopularCurrencies()).isEmpty();
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage: валидная валюта")
    void buildCurrencyToRubMessage_valid() {
        Currency usd = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        String msg = service.buildCurrencyToRubMessage(usd);

        assertThat(msg).contains("Курс валюты к рублю");
        assertThat(msg).contains("USD");
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage: null валюта")
    void buildCurrencyToRubMessage_null() {
        assertThatThrownBy(() -> service.buildCurrencyToRubMessage(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage: валюта без кода/имени/курса")
    void buildCurrencyToRubMessage_incompleteCurrency() {
        Currency c = new Currency();
        assertThatThrownBy(() -> service.buildCurrencyToRubMessage(c))
                .isInstanceOf(NullPointerException.class);
    }
} 