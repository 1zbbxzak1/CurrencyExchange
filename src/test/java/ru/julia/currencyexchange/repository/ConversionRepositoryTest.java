package ru.julia.currencyexchange.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.entity.CurrencyConversion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionRepositoryTest {

    @Mock
    private ConversionRepository conversionRepository;

    @Test
    void testFindByCurrencyCodeAndDate() {
        // Подготовка данных
        Currency usd = new Currency("USD", "United States Dollar", BigDecimal.valueOf(75.5));
        LocalDate today = LocalDate.now();
        CurrencyConversion conversion = new CurrencyConversion();
        conversion.setSourceCurrency(usd);

        // Мокируем поведение репозитория
        when(conversionRepository.findByCurrencyCodeAndDate(usd, today)).thenReturn(List.of(conversion));

        // Вызываем метод и проверяем результат
        List<CurrencyConversion> result = conversionRepository.findByCurrencyCodeAndDate(usd, today);
        assertEquals(1, result.size());
        assertEquals("USD", result.getFirst().getSourceCurrency().getCode());

        // Проверяем, что метод вызывался 1 раз
        verify(conversionRepository, times(1)).findByCurrencyCodeAndDate(usd, today);
    }

    @Test
    void testFindByCurrencyCodeAndDate_NotFound() {
        // Подготовка данных
        Currency eur = new Currency("EUR", "Euro", BigDecimal.valueOf(80.2));
        LocalDate today = LocalDate.now();

        // Мокируем пустой ответ
        when(conversionRepository.findByCurrencyCodeAndDate(eur, today)).thenReturn(Collections.emptyList());

        // Вызываем метод и проверяем, что список пуст
        List<CurrencyConversion> result = conversionRepository.findByCurrencyCodeAndDate(eur, today);
        assertTrue(result.isEmpty());

        // Проверяем вызов метода
        verify(conversionRepository, times(1)).findByCurrencyCodeAndDate(eur, today);
    }

    @Test
    void testFindConversionsByUserId() {
        String userId = "user-123";
        CurrencyConversion conversion1 = new CurrencyConversion();
        CurrencyConversion conversion2 = new CurrencyConversion();

        // Мокируем поведение репозитория
        when(conversionRepository.findConversionByUserId(userId)).thenReturn(List.of(conversion1, conversion2));

        // Вызываем метод и проверяем результат
        List<CurrencyConversion> result = conversionRepository.findConversionByUserId(userId);
        assertEquals(2, result.size());

        // Проверяем, что метод был вызван
        verify(conversionRepository, times(1)).findConversionByUserId(userId);
    }

    @Test
    void testFindConversionsByUserId_NotFound() {
        String userId = "non-existing-user";

        // Мокируем поведение репозитория (пустой список)
        when(conversionRepository.findConversionByUserId(userId)).thenReturn(Collections.emptyList());

        // Вызываем метод и проверяем, что список пуст
        List<CurrencyConversion> result = conversionRepository.findConversionByUserId(userId);
        assertTrue(result.isEmpty());

        // Проверяем вызов метода
        verify(conversionRepository, times(1)).findConversionByUserId(userId);
    }
}
