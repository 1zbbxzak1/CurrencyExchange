package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfile
@PostgresTestcontainers
class ConversionRepositoryIntegrationTest {
    @Autowired
    private ConversionRepository conversionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск по дате и userId")
    void findByCurrencyDate() {
        User user = new User();
        user.setUsername("convuser");
        user.setEmail("conv@example.com");
        user.setChatId(44444L);
        user.setPassword("testpass");
        userRepository.save(user);

        Currency currency = new Currency();
        currency.setCode("GBP");
        currency.setName("Pound");
        currency.setExchangeRate(BigDecimal.valueOf(120.0));
        currencyRepository.save(currency);

        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setUser(user);
        conversion.setSourceCurrency(currency);
        conversion.setTargetCurrency(currency);
        conversion.setAmount(BigDecimal.valueOf(100));
        conversion.setConvertedAmount(BigDecimal.valueOf(12000));
        conversion.setConversionRate(BigDecimal.valueOf(120.0));
        conversionRepository.save(conversion);

        List<CurrencyConversion> found = conversionRepository.findByCurrencyDate(LocalDate.now(), user.getId());

        assertThat(found).isNotEmpty();
        assertThat(found.getFirst().getUser().getUsername()).isEqualTo("convuser");
    }

    @Test
    @DisplayName("Поиск конвертаций по userId")
    void findConversionByUserId() {
        User user = new User();
        user.setUsername("convuser2");
        user.setEmail("conv2@example.com");
        user.setChatId(55555L);
        user.setPassword("testpass");
        userRepository.save(user);

        Currency currency = new Currency();
        currency.setCode("JPY");
        currency.setName("Yen");
        currency.setExchangeRate(BigDecimal.valueOf(0.5));
        currencyRepository.save(currency);

        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setUser(user);
        conversion.setSourceCurrency(currency);
        conversion.setTargetCurrency(currency);
        conversion.setAmount(BigDecimal.valueOf(200));
        conversion.setConvertedAmount(BigDecimal.valueOf(100));
        conversion.setConversionRate(BigDecimal.valueOf(0.5));
        conversionRepository.save(conversion);

        List<CurrencyConversion> found = conversionRepository.findConversionByUserId(user.getId());

        assertThat(found).isNotEmpty();
        assertThat(found.getFirst().getUser().getUsername()).isEqualTo("convuser2");
    }

    @Test
    @DisplayName("Поиск несуществующей конверсии возвращает empty")
    void findByIdNotFound() {
        Optional<CurrencyConversion> found = conversionRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление конверсии")
    void deleteConversion() {
        User user = new User();
        user.setUsername("delconv");
        user.setEmail("delconv@example.com");
        user.setChatId(123123L);
        user.setPassword("pass");
        userRepository.save(user);

        Currency currency = new Currency();
        currency.setCode("DEL");
        currency.setName("DelCurr");
        currency.setExchangeRate(BigDecimal.valueOf(1));
        currencyRepository.save(currency);

        CurrencyConversion conv = new CurrencyConversion();
        conv.setUser(user);
        conv.setSourceCurrency(currency);
        conv.setTargetCurrency(currency);
        conv.setAmount(BigDecimal.valueOf(1));
        conv.setConvertedAmount(BigDecimal.valueOf(1));
        conv.setConversionRate(BigDecimal.valueOf(1));
        conversionRepository.save(conv);
        conversionRepository.deleteById(conv.getId());

        assertThat(conversionRepository.findById(conv.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление конверсии")
    void updateConversion() {
        User user = new User();
        user.setUsername("updconv");
        user.setEmail("updconv@example.com");
        user.setChatId(321321L);
        user.setPassword("pass");
        userRepository.save(user);

        Currency currency = new Currency();
        currency.setCode("UPD");
        currency.setName("UpdCurr");
        currency.setExchangeRate(BigDecimal.valueOf(1));
        currencyRepository.save(currency);

        CurrencyConversion conv = new CurrencyConversion();
        conv.setUser(user);
        conv.setSourceCurrency(currency);
        conv.setTargetCurrency(currency);
        conv.setAmount(BigDecimal.valueOf(1));
        conv.setConvertedAmount(BigDecimal.valueOf(1));
        conv.setConversionRate(BigDecimal.valueOf(1));
        conversionRepository.save(conv);
        conv.setAmount(BigDecimal.valueOf(2));
        conversionRepository.save(conv);

        Optional<CurrencyConversion> found = conversionRepository.findById(conv.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(BigDecimal.valueOf(2));
    }

    @Test
    @DisplayName("findAll возвращает все конверсии")
    void findAllReturnsAllConversions() {
        User user = new User();
        user.setUsername("userconv");
        user.setEmail("userconv@example.com");
        user.setChatId(77777L);
        user.setPassword("testpass");
        userRepository.save(user);

        Currency currency = new Currency();
        currency.setCode("CHF");
        currency.setName("Franc");
        currency.setExchangeRate(BigDecimal.valueOf(95.0));
        currencyRepository.save(currency);

        CurrencyConversion conv1 = new CurrencyConversion();
        conv1.setUser(user);
        conv1.setSourceCurrency(currency);
        conv1.setTargetCurrency(currency);
        conv1.setAmount(BigDecimal.valueOf(10));
        conv1.setConvertedAmount(BigDecimal.valueOf(950));
        conv1.setConversionRate(BigDecimal.valueOf(95.0));

        CurrencyConversion conv2 = new CurrencyConversion();
        conv2.setUser(user);
        conv2.setSourceCurrency(currency);
        conv2.setTargetCurrency(currency);
        conv2.setAmount(BigDecimal.valueOf(20));
        conv2.setConvertedAmount(BigDecimal.valueOf(1900));
        conv2.setConversionRate(BigDecimal.valueOf(95.0));

        conversionRepository.save(conv1);
        conversionRepository.save(conv2);

        Iterable<CurrencyConversion> conversions = conversionRepository.findAll();

        assertThat(conversions).anyMatch(c -> c.getAmount().intValue() == 10 || c.getAmount().intValue() == 20);
    }
}