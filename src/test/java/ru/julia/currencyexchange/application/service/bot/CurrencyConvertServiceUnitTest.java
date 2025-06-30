package ru.julia.currencyexchange.application.service.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

class CurrencyConvertServiceUnitTest {
    @Mock
    private CurrencyExchangeService currencyExchangeService;
    @Mock
    private UserService userService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private CurrencyEmojiUtils currencyEmojiUtils;
    @Mock
    private CurrencyFormatUtils currencyFormatUtils;
    @Mock
    private MessageConverter messageConverter;
    private CurrencyConvertService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CurrencyConvertService(
                currencyExchangeService,
                userService,
                settingsService,
                currencyEmojiUtils,
                currencyFormatUtils,
                messageConverter
        );
    }

    @Test
    @DisplayName("hasCurrencies возвращает true, если есть валюты")
    void hasCurrencies_true() {
        when(currencyExchangeService.getAllCurrencies()).thenReturn(List.of(new Currency()));
        assertTrue(service.hasCurrencies());
    }

    @Test
    @DisplayName("hasCurrencies возвращает false, если валют нет")
    void hasCurrencies_false() {
        when(currencyExchangeService.getAllCurrencies()).thenReturn(List.of());
        assertFalse(service.hasCurrencies());
    }

    @Test
    @DisplayName("convertCurrency: пользователь найден, конвертация успешна")
    void convertCurrency_success() {
        User user = new User();
        setUserId(user, "1");
        when(userService.findUserByChatId(10L)).thenReturn(user);
        CurrencyConversion conversion = mock(CurrencyConversion.class);
        when(currencyExchangeService.convert("1", "USD", "RUB", BigDecimal.TEN)).thenReturn(conversion);
        CurrencyConversion result = service.convertCurrency(10L, "USD", "RUB", BigDecimal.TEN);
        assertThat(result).isEqualTo(conversion);
    }

    private void setUserId(User user, String id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("convertCurrency: пользователь не найден — выбрасывается IllegalArgumentException")
    void convertCurrency_userNotFound() {
        when(userService.findUserByChatId(10L)).thenReturn(null);
        when(messageConverter.resolve(anyString())).thenReturn("not found");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.convertCurrency(10L, "USD", "RUB", BigDecimal.TEN));
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    @DisplayName("getCurrencyByCode делегирует currencyExchangeService")
    void getCurrencyByCode_delegates() {
        Currency currency = new Currency();
        when(currencyExchangeService.getCurrencyByCode("USD")).thenReturn(currency);
        assertThat(service.getCurrencyByCode("USD")).isEqualTo(currency);
    }

    @Test
    @DisplayName("buildConversionMessage формирует корректное сообщение")
    void buildConversionMessage_success() {
        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        setLastUpdated(from, java.time.LocalDateTime.now());
        Currency to = new Currency("RUB", "Рубль", BigDecimal.valueOf(50));
        setLastUpdated(to, java.time.LocalDateTime.now());
        CurrencyConversion conversion = mock(CurrencyConversion.class);
        when(conversion.getSourceCurrency()).thenReturn(from);
        when(conversion.getTargetCurrency()).thenReturn(to);
        when(conversion.getAmount()).thenReturn(BigDecimal.TEN);
        when(conversion.getConvertedAmount()).thenReturn(BigDecimal.valueOf(500));
        when(settingsService.getGlobalConversionFeePercent()).thenReturn(0.0);
        when(messageConverter.resolve(anyString())).thenReturn("msg");
        when(messageConverter.resolve(anyString(), anyMap())).thenReturn("msg");
        when(currencyEmojiUtils.getCurrencyEmoji(anyString())).thenReturn(":)");
        when(currencyFormatUtils.formatAmount(any())).thenReturn("10");
        when(currencyFormatUtils.formatExchangeRate(any())).thenReturn("100");
        String result = service.buildConversionMessage(conversion);
        assertThat(result).contains("msg");
    }

    private void setLastUpdated(Currency currency, java.time.LocalDateTime dateTime) {
        try {
            Field field = Currency.class.getDeclaredField("lastUpdated");
            field.setAccessible(true);
            field.set(currency, dateTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("isRub возвращает true для RUB")
    void isRub_true() {
        assertTrue(service.isRub("RUB"));
        assertTrue(service.isRub("rub"));
    }

    @Test
    @DisplayName("isRub возвращает false для других валют")
    void isRub_false() {
        assertFalse(service.isRub("USD"));
    }

    @Test
    @DisplayName("isValidAmount: валидное положительное число")
    void isValidAmount_valid() {
        assertTrue(service.isValidAmount("100.00"));
    }

    @Test
    @DisplayName("isValidAmount: невалидное число")
    void isValidAmount_invalid() {
        assertFalse(service.isValidAmount("-1"));
        assertFalse(service.isValidAmount("abc"));
    }

    @Test
    @DisplayName("parseAmount корректно парсит строку")
    void parseAmount_success() {
        assertThat(service.parseAmount("123,45")).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    @DisplayName("getState возвращает NONE по умолчанию")
    void getState_defaultNone() {
        assertThat(service.getState(1L)).isEqualTo(ConversionState.NONE);
    }

    @Test
    @DisplayName("setState и getState работают корректно")
    void setState_and_getState() {
        service.setState(2L, ConversionState.WAITING_AMOUNT);
        assertThat(service.getState(2L)).isEqualTo(ConversionState.WAITING_AMOUNT);
    }

    @Test
    @DisplayName("setData/getData/clearData работают корректно")
    void setData_getData_clearData() {
        service.setData(3L, "USD", "RUB");
        CurrencyConvertService.ConversionData data = service.getData(3L);
        assertThat(data.fromCurrency()).isEqualTo("USD");
        assertThat(data.toCurrency()).isEqualTo("RUB");
        service.clearData(3L);
        assertThat(service.getData(3L)).isNull();
        assertThat(service.getState(3L)).isEqualTo(ConversionState.NONE);
    }
} 