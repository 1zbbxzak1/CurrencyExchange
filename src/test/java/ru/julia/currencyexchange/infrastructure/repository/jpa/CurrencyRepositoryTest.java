package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfile
@PostgresTestcontainers
class CurrencyRepositoryTest {
    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск по id")
    void saveAndFindById() {
        Currency currency = new Currency();
        currency.setCode("USD");
        currency.setName("US Dollar");
        currency.setExchangeRate(java.math.BigDecimal.valueOf(90.0));
        currencyRepository.save(currency);

        Optional<Currency> found = currencyRepository.findById(currency.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("US Dollar");
    }

    @Test
    @DisplayName("Поиск по коду валюты")
    void findByCode() {
        Currency currency = new Currency();
        currency.setCode("EUR");
        currency.setName("Euro");
        currency.setExchangeRate(java.math.BigDecimal.valueOf(100.0));
        currencyRepository.save(currency);

        Optional<Currency> found = currencyRepository.findByCode("EUR");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Euro");
    }

    @Test
    @DisplayName("Поиск несуществующей валюты возвращает empty")
    void findByIdNotFound() {
        Optional<Currency> found = currencyRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление валюты")
    void deleteCurrency() {
        Currency currency = new Currency();
        currency.setCode("TO_DELETE");
        currency.setName("To Delete");
        currency.setExchangeRate(java.math.BigDecimal.valueOf(1));
        currencyRepository.save(currency);
        currencyRepository.deleteById(currency.getId());
        assertThat(currencyRepository.findById(currency.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление валюты")
    void updateCurrency() {
        Currency currency = new Currency();
        currency.setCode("TO_UPDATE");
        currency.setName("To Update");
        currency.setExchangeRate(java.math.BigDecimal.valueOf(1));
        currencyRepository.save(currency);
        currency.setName("Updated");
        currencyRepository.save(currency);
        Optional<Currency> found = currencyRepository.findById(currency.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("findAll возвращает все валюты")
    void findAllReturnsAllCurrencies() {
        Currency currency1 = new Currency();
        currency1.setCode("USD");
        currency1.setName("US Dollar");
        currency1.setExchangeRate(java.math.BigDecimal.valueOf(90.0));
        Currency currency2 = new Currency();
        currency2.setCode("EUR");
        currency2.setName("Euro");
        currency2.setExchangeRate(java.math.BigDecimal.valueOf(100.0));
        currencyRepository.save(currency1);
        currencyRepository.save(currency2);
        Iterable<Currency> currencies = currencyRepository.findAll();
        assertThat(currencies).extracting(Currency::getCode).contains("USD", "EUR");
    }
} 