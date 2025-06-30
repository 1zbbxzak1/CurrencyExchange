package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.exceptions.CurrencyNotFoundException;
import ru.julia.currencyexchange.application.exceptions.InvalidDateFormatException;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ConversionRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CurrencyExchangeServiceUnitTest {
    @Mock
    private CurrencyService currencyService;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ConversionRepository conversionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SettingsService settingsService;
    @InjectMocks
    private CurrencyExchangeService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("Успешная конвертация валюты без комиссии")
    void convert_success_noFee() {
        User user = new User();
        setId(user, "1");

        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        Currency to = new Currency("RUB", "Рубль", BigDecimal.valueOf(50));
        BigDecimal amount = BigDecimal.valueOf(10);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(from));
        when(currencyRepository.findByCode("RUB")).thenReturn(Optional.of(to));
        when(settingsService.getGlobalConversionFeePercent()).thenReturn(0.0);
        when(conversionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CurrencyConversion result = service.convert("1", "USD", "RUB", amount);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getSourceCurrency()).isEqualTo(from);
        assertThat(result.getTargetCurrency()).isEqualTo(to);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getConversionRate()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
    }

    private static void setId(Object entity, String id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Успешная конвертация валюты с комиссией")
    void convert_success_withFee() {
        User user = new User();
        setId(user, "1");

        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        Currency to = new Currency("RUB", "Рубль", BigDecimal.valueOf(50));
        BigDecimal amount = BigDecimal.valueOf(10);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(from));
        when(currencyRepository.findByCode("RUB")).thenReturn(Optional.of(to));
        when(settingsService.getGlobalConversionFeePercent()).thenReturn(10.0);
        when(conversionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CurrencyConversion result = service.convert("1", "USD", "RUB", amount);
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(BigDecimal.valueOf(18.0));
    }

    @Test
    @DisplayName("Деление на 0 при курсе целевой валюты")
    void convert_divideByZero() {
        User user = new User();
        setId(user, "1");

        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        Currency to = new Currency("RUB", "Рубль", BigDecimal.ZERO);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(from));
        when(currencyRepository.findByCode("RUB")).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> service.convert("1", "USD", "RUB", BigDecimal.TEN))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");
    }

    @Test
    @DisplayName("Пользователь не найден")
    void convert_userNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert("1", "USD", "RUB", BigDecimal.TEN))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Исходная валюта не найдена")
    void convert_fromCurrencyNotFound() {
        User user = new User();
        setId(user, "1");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert("1", "USD", "RUB", BigDecimal.TEN))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    @DisplayName("Целевая валюта не найдена")
    void convert_toCurrencyNotFound() {
        User user = new User();
        setId(user, "1");

        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(100));

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(from));
        when(currencyRepository.findByCode("RUB")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.convert("1", "USD", "RUB", BigDecimal.TEN))
                .isInstanceOf(CurrencyNotFoundException.class);
    }

    @Test
    @DisplayName("История пользователя")
    void getUserHistory_success() {
        User user = new User();
        setId(user, "1");

        List<CurrencyConversion> history = List.of(mock(CurrencyConversion.class));

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(conversionRepository.findConversionByUserId("1")).thenReturn(history);

        List<CurrencyConversion> result = service.getUserHistory("1");
        assertThat(result).isEqualTo(history);
    }

    @Test
    @DisplayName("История пользователя: не найден")
    void getUserHistory_userNotFound() {
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserHistory("1"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Поиск по дате: успешный сценарий")
    void findByCurrencyDate_success() {
        List<CurrencyConversion> list = List.of(mock(CurrencyConversion.class));
        when(conversionRepository.findByCurrencyDate(any(LocalDate.class), eq("1"))).thenReturn(list);

        List<CurrencyConversion> result = service.findByCurrencyDate("1", "2024-06-30");
        assertThat(result).isEqualTo(list);
    }

    @Test
    @DisplayName("Поиск по дате: невалидная дата")
    void findByCurrencyDate_invalidDate() {
        assertThatThrownBy(() -> service.findByCurrencyDate("1", "bad-date"))
                .isInstanceOf(InvalidDateFormatException.class);
    }

    @Test
    @DisplayName("Обновление курсов делегируется CurrencyService")
    void updateCurrencyRates_delegates() {
        List<Currency> list = List.of(mock(Currency.class));
        when(currencyService.updateExchangeRates()).thenReturn(list);

        List<Currency> result = service.updateCurrencyRates("1");
        assertThat(result).isEqualTo(list);
    }

    @Test
    @DisplayName("Получение всех валют")
    void getAllCurrencies_success() {
        Currency c1 = new Currency("USD", "Доллар", BigDecimal.ONE);
        Currency c2 = new Currency("RUB", "Рубль", BigDecimal.TEN);
        when(currencyRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Currency> result = service.getAllCurrencies();
        assertThat(result).containsExactly(c1, c2);
    }

    @Test
    @DisplayName("Получение валюты по коду: найдена")
    void getCurrencyByCode_found() {
        Currency c = new Currency("USD", "Доллар", BigDecimal.ONE);
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(c));

        Currency result = service.getCurrencyByCode("USD");
        assertThat(result).isEqualTo(c);
    }

    @Test
    @DisplayName("Получение валюты по коду: не найдена")
    void getCurrencyByCode_notFound() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        Currency result = service.getCurrencyByCode("USD");
        assertThat(result).isNull();
    }
} 