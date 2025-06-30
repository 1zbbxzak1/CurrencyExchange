package ru.julia.currencyexchange.application.service.bot.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class CurrencyConvertServiceIntegrationTest {
    @Autowired
    private CurrencyConvertService service;
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
    @DisplayName("hasCurrencies: true/false")
    void hasCurrencies() {
        assertThat(service.hasCurrencies()).isFalse();

        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));

        assertThat(service.hasCurrencies()).isTrue();
    }

    @Test
    @DisplayName("getAllCurrencies и getPopularCurrencies")
    void getAllAndPopularCurrencies() {
        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));
        currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.TEN));
        currencyRepository.save(new Currency("ABC", "Тест", BigDecimal.ONE));

        List<Currency> all = service.getAllCurrencies();
        List<Currency> popular = service.getPopularCurrencies();

        assertThat(all).hasSize(3);
        assertThat(popular).extracting(Currency::getCode).contains("USD", "RUB").doesNotContain("ABC");
    }

    @Test
    @DisplayName("convertCurrency: success и user not found")
    void convertCurrency_success_and_userNotFound() {
        User user = new User("user@mail.com", "pass");
        user.setChatId(1L);
        user.setUsername("user");
        userRepository.save(user);
        Currency from = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        Currency to = currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.valueOf(50)));
        CurrencyConversion conv = service.convertCurrency(1L, "USD", "RUB", BigDecimal.TEN);

        assertThat(conv.getUser().getId()).isEqualTo(user.getId());
        assertThat(conv.getSourceCurrency().getCode()).isEqualTo("USD");
        assertThat(conv.getTargetCurrency().getCode()).isEqualTo("RUB");
        assertThat(conv.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(conv.getConvertedAmount()).isPositive();

        assertThatThrownBy(() -> service.convertCurrency(999L, "USD", "RUB", BigDecimal.ONE))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getCurrencyByCode: found/not found")
    void getCurrencyByCode() {
        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.ONE));

        assertThat(service.getCurrencyByCode("USD")).isNotNull();
        assertThat(service.getCurrencyByCode("XXX")).isNull();
    }

    @Test
    @DisplayName("buildConversionMessage: с комиссией и без")
    void buildConversionMessage() {
        User user = new User("user@mail.com", "pass");
        user.setChatId(2L);
        user.setUsername("user");
        userRepository.save(user);
        Currency from = currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        Currency to = currencyRepository.save(new Currency("RUB", "Рубль", BigDecimal.valueOf(50)));
        settingsService.setGlobalConversionFee(0.0);
        CurrencyConversion conv = service.convertCurrency(2L, "USD", "RUB", BigDecimal.TEN);
        String msg = service.buildConversionMessage(conv);

        assertThat(msg).contains("Конвертация валют");

        settingsService.setGlobalConversionFee(2.5);
        CurrencyConversion conv2 = service.convertCurrency(2L, "USD", "RUB", BigDecimal.TEN);
        String msg2 = service.buildConversionMessage(conv2);

        assertThat(msg2).contains("Комиссия");
    }

    @Test
    @DisplayName("isRub: true/false")
    void isRub() {
        assertThat(service.isRub("RUB")).isTrue();
        assertThat(service.isRub("rub")).isTrue();
        assertThat(service.isRub("USD")).isFalse();
    }

    @Test
    @DisplayName("isValidAmount: валидные и невалидные значения")
    void isValidAmount() {
        assertThat(service.isValidAmount("100.00")).isTrue();
        assertThat(service.isValidAmount("-1")).isFalse();
        assertThat(service.isValidAmount("abc")).isFalse();
        assertThat(service.isValidAmount("1000000000")).isFalse();
    }

    @Test
    @DisplayName("parseAmount: корректный парсинг")
    void parseAmount() {
        assertThat(service.parseAmount("123,45")).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(service.parseAmount("123.45")).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    @DisplayName("get/set/clear state и data")
    void stateAndData() {
        Long chatId = 10L;
        assertThat(service.getState(chatId)).isEqualTo(ConversionState.NONE);

        service.setState(chatId, ConversionState.WAITING_AMOUNT);
        assertThat(service.getState(chatId)).isEqualTo(ConversionState.WAITING_AMOUNT);

        service.setData(chatId, "USD", "RUB");
        var data = service.getData(chatId);
        assertThat(data.fromCurrency()).isEqualTo("USD");
        assertThat(data.toCurrency()).isEqualTo("RUB");

        service.clearData(chatId);
        assertThat(service.getData(chatId)).isNull();
        assertThat(service.getState(chatId)).isEqualTo(ConversionState.NONE);
    }
} 