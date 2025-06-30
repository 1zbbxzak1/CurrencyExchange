package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import ru.julia.currencyexchange.application.dto.CurrencyRate;
import ru.julia.currencyexchange.application.dto.CurrencyRatesList;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateFetchException;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateParsingException;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateSaveException;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CurrencyServiceUnitTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("Успешное обновление курсов валют (RUB + одна валюта)")
    void updateExchangeRates_success() {
        CurrencyRate rub = new CurrencyRate("RUB", 1, "Российский рубль", "1.0");
        CurrencyRate usd = new CurrencyRate("USD", 1, "Доллар США", "90.0");
        CurrencyRatesList ratesList = mock(CurrencyRatesList.class);

        when(ratesList.getValute()).thenReturn(List.of(usd));

        String xml = "xml";
        CurrencyService spyService = spy(currencyService);

        doReturn(xml).when(spyService).fetchCurrencyRatesXml();
        doReturn(Map.of("RUB", rub, "USD", usd)).when(spyService).parseCurrencyRates(xml);

        doNothing().when(spyService).saveCurrencyRates(anyMap());

        when(currencyRepository.findAll()).thenReturn(List.of(new Currency("RUB", "Российский рубль", BigDecimal.ONE), new Currency("USD", "Доллар США", BigDecimal.valueOf(90.0))));

        List<Currency> result = spyService.updateExchangeRates();

        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(c -> c.getCode().equals("USD") && c.getExchangeRate().compareTo(BigDecimal.valueOf(90.0)) == 0);
    }

    @Test
    @DisplayName("Ошибка при получении курсов валют")
    void updateExchangeRates_fetchError() {
        CurrencyService spyService = spy(currencyService);

        doThrow(new CurrencyRateFetchException("fail")).when(spyService).fetchCurrencyRatesXml();

        assertThatThrownBy(spyService::updateExchangeRates)
                .isInstanceOf(CurrencyRateSaveException.class)
                .hasMessageContaining("Не удалось обновить курсы валют");
    }

    @Test
    @DisplayName("Ошибка парсинга XML")
    void updateExchangeRates_parseError() {
        CurrencyService spyService = spy(currencyService);

        doReturn("xml").when(spyService).fetchCurrencyRatesXml();

        doThrow(new CurrencyRateParsingException("parse fail")).when(spyService).parseCurrencyRates(anyString());

        assertThatThrownBy(spyService::updateExchangeRates)
                .isInstanceOf(CurrencyRateSaveException.class)
                .hasMessageContaining("Не удалось обновить курсы валют");
    }

    @Test
    @DisplayName("Ошибка сохранения валюты в БД")
    void updateExchangeRates_saveError() {
        CurrencyRate rub = new CurrencyRate("RUB", 1, "Российский рубль", "1.0");
        CurrencyRatesList ratesList = mock(CurrencyRatesList.class);

        when(ratesList.getValute()).thenReturn(List.of());

        String xml = "xml";
        CurrencyService spyService = spy(currencyService);

        doReturn(xml).when(spyService).fetchCurrencyRatesXml();
        doReturn(Map.of("RUB", rub)).when(spyService).parseCurrencyRates(xml);

        doThrow(new DataIntegrityViolationException("db fail")).when(spyService).saveCurrencyRates(anyMap());

        assertThatThrownBy(spyService::updateExchangeRates)
                .isInstanceOf(CurrencyRateSaveException.class)
                .hasMessageContaining("Не удалось обновить курсы валют");
    }

    @Test
    @DisplayName("Обновление существующей валюты")
    void updateExchangeRates_updateExistingCurrency() {
        CurrencyRate usd = new CurrencyRate("USD", 1, "Доллар США", "100.0");
        Currency existing = new Currency("USD", "Доллар США", BigDecimal.valueOf(90.0));

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(existing));
        when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        CurrencyService service = new CurrencyService(currencyRepository);
        Map<String, CurrencyRate> rates = Map.of("USD", usd);
        service.saveCurrencyRates(rates);

        assertThat(existing.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(existing.getName()).isEqualTo("Доллар США");
    }
}
