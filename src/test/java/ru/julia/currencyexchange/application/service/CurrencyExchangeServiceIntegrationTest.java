package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.InvalidDateFormatException;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ConversionRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class CurrencyExchangeServiceIntegrationTest {
    @Autowired
    private CurrencyExchangeService service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ConversionRepository conversionRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private SettingsService settingsService;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Пользователь не найден")
    void convert_userNotFound() {
        assertThatThrownBy(() -> service.convert("nonexistent", "USD", "RUB", BigDecimal.TEN))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("История пользователя")
    void getUserHistory_success() {
        User user = new User();
        user.setChatId(123456789L);
        user.setEmail("ituser@mail.com");
        user.setPassword("pass");
        user.setUsername("testuser");
        userRepository.save(user);
        Currency from = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        Currency to = currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.valueOf(50)));
        CurrencyConversion conv = conversionRepository.save(new CurrencyConversion(user, from, to, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE));
        List<CurrencyConversion> result = service.getUserHistory(user.getId());
        assertThat(result).extracting("id").contains(conv.getId());
    }

    @Test
    @DisplayName("История пользователя: не найден")
    void getUserHistory_userNotFound() {
        assertThatThrownBy(() -> service.getUserHistory("nonexistent"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Поиск по дате: успешный сценарий")
    void findByCurrencyDate_success() {
        User user = new User();
        user.setChatId(123456789L);
        user.setEmail("ituser@mail.com");
        user.setPassword("pass");
        user.setUsername("testuser");
        userRepository.save(user);
        Currency from = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        Currency to = currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.valueOf(50)));
        CurrencyConversion conv = conversionRepository.save(new CurrencyConversion(user, from, to, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE));
        String today = LocalDate.now().toString();
        List<CurrencyConversion> result = service.findByCurrencyDate(user.getId(), today);
        assertThat(result).extracting("id").contains(conv.getId());
    }

    @Test
    @DisplayName("Поиск по дате: невалидная дата")
    void findByCurrencyDate_invalidDate() {
        User user = new User();
        user.setChatId(123456789L);
        user.setEmail("ituser@mail.com");
        user.setPassword("pass");
        user.setUsername("testuser");
        userRepository.save(user);
        assertThatThrownBy(() -> service.findByCurrencyDate(user.getId(), "bad-date"))
                .isInstanceOf(InvalidDateFormatException.class);
    }

    @Test
    @DisplayName("Обновление курсов делегируется CurrencyService (Smoke)")
    void updateCurrencyRates_delegates() {
        // Просто smoke-тест, что метод не падает
        service.updateCurrencyRates("any");
    }

    @Test
    @DisplayName("Получение всех валют")
    void getAllCurrencies_success() {
        Currency c1 = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));
        Currency c2 = currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.TEN));
        List<Currency> result = service.getAllCurrencies();
        assertThat(result).extracting("code").containsExactlyInAnyOrder("USD", "RUB");
    }

    @Test
    @DisplayName("Получение валюты по коду: найдена")
    void getCurrencyByCode_found() {
        Currency c = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));
        Currency result = service.getCurrencyByCode("USD");
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Получение валюты по коду: не найдена")
    void getCurrencyByCode_notFound() {
        Currency result = service.getCurrencyByCode("USD");
        assertThat(result).isNull();
    }
} 