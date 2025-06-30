package ru.julia.currencyexchange.application.service.bot.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CurrencyToRubServiceUnitTest {
    @Mock
    private CurrencyExchangeService currencyExchangeService;
    @Mock
    private CurrencyEmojiUtils currencyEmojiUtils;
    @Mock
    private CurrencyFormatUtils currencyFormatUtils;
    @Mock
    private MessageConverter messageConverter;
    private CurrencyToRubService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new CurrencyToRubService(
                currencyExchangeService,
                currencyEmojiUtils,
                currencyFormatUtils,
                messageConverter
        );
    }

    @Test
    @DisplayName("getCurrencyByCode делегирует currencyExchangeService")
    void getCurrencyByCode_delegates() {
        Currency currency = new Currency();

        when(currencyExchangeService.getCurrencyByCode("USD")).thenReturn(currency);
        assertThat(service.getCurrencyByCode("USD")).isEqualTo(currency);
    }

    @Test
    @DisplayName("hasCurrencies: true/false")
    void hasCurrencies() {
        when(currencyExchangeService.getAllCurrencies()).thenReturn(List.of(new Currency()));
        assertThat(service.hasCurrencies()).isTrue();

        when(currencyExchangeService.getAllCurrencies()).thenReturn(emptyList());
        assertThat(service.hasCurrencies()).isFalse();
    }

    @Test
    @DisplayName("getAllCurrencies делегирует currencyExchangeService")
    void getAllCurrencies_delegates() {
        List<Currency> list = List.of(new Currency());

        when(currencyExchangeService.getAllCurrencies()).thenReturn(list);
        assertThat(service.getAllCurrencies()).isEqualTo(list);
    }

    @Test
    @DisplayName("getPopularCurrencies фильтрует только популярные")
    void getPopularCurrencies_filtersPopular() {
        Currency usd = new Currency("USD", "Доллар", BigDecimal.ONE);
        Currency rub = new Currency("RUB", "Рубль", BigDecimal.TEN);
        Currency abc = new Currency("ABC", "Тест", BigDecimal.ONE);

        when(currencyExchangeService.getAllCurrencies()).thenReturn(List.of(usd, rub, abc));

        List<Currency> popular = service.getPopularCurrencies();
        assertThat(popular).extracting(Currency::getCode).contains("USD", "RUB").doesNotContain("ABC");
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage формирует корректное сообщение")
    void buildCurrencyToRubMessage_success() {
        Currency currency = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        setLastUpdated(currency, LocalDateTime.now());

        when(currencyEmojiUtils.getCurrencyEmoji(anyString())).thenReturn(":)");
        when(currencyFormatUtils.formatExchangeRate(any())).thenReturn("100");
        when(messageConverter.resolve(anyString())).thenReturn("msg");
        when(messageConverter.resolve(anyString(), anyMap())).thenReturn("msg");

        String result = service.buildCurrencyToRubMessage(currency);
        assertThat(result).contains("msg");
    }

    private void setLastUpdated(Currency currency, LocalDateTime dateTime) {
        try {
            var field = Currency.class.getDeclaredField("lastUpdated");
            field.setAccessible(true);
            field.set(currency, dateTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getCurrencyByCode возвращает null, если не найдено")
    void getCurrencyByCode_notFound() {
        when(currencyExchangeService.getCurrencyByCode("XXX")).thenReturn(null);
        assertThat(service.getCurrencyByCode("XXX")).isNull();
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage выбрасывает NPE, если currency == null")
    void buildCurrencyToRubMessage_nullCurrency() {
        assertThatThrownBy(() -> service.buildCurrencyToRubMessage(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("getPopularCurrencies возвращает пустой список, если валюты пусты или нет популярных")
    void getPopularCurrencies_emptyOrNoPopular() {
        when(currencyExchangeService.getAllCurrencies()).thenReturn(emptyList());
        assertThat(service.getPopularCurrencies()).isEmpty();

        Currency abc = new Currency("ABC", "Тест", BigDecimal.ONE);
        when(currencyExchangeService.getAllCurrencies()).thenReturn(List.of(abc));
        assertThat(service.getPopularCurrencies()).isEmpty();
    }

    @Test
    @DisplayName("hasCurrencies возвращает false, если getAllCurrencies возвращает null")
    void hasCurrencies_nullList() {
        when(currencyExchangeService.getAllCurrencies()).thenReturn(null);
        assertThat(service.hasCurrencies()).isFalse();
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage: messageConverter возвращает null")
    void buildCurrencyToRubMessage_messageConverterReturnsNull() {
        Currency currency = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        setLastUpdated(currency, LocalDateTime.now());

        when(currencyEmojiUtils.getCurrencyEmoji(anyString())).thenReturn(":)");
        when(currencyFormatUtils.formatExchangeRate(any())).thenReturn("100");
        when(messageConverter.resolve(anyString())).thenReturn(null);
        when(messageConverter.resolve(anyString(), any())).thenReturn(null);

        String result = service.buildCurrencyToRubMessage(currency);
        assertThat(result).contains("null");
    }

    @Test
    @DisplayName("buildCurrencyToRubMessage: messageConverter кидает исключение")
    void buildCurrencyToRubMessage_messageConverterThrows() {
        Currency currency = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        setLastUpdated(currency, LocalDateTime.now());

        when(currencyEmojiUtils.getCurrencyEmoji(anyString())).thenReturn(":)");
        when(currencyFormatUtils.formatExchangeRate(any())).thenReturn("100");
        when(messageConverter.resolve(anyString())).thenThrow(new RuntimeException("fail"));
        when(messageConverter.resolve(anyString(), any())).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> service.buildCurrencyToRubMessage(currency))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fail");
    }
} 