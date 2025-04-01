package ru.julia.currencyexchange.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.entity.Currency;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyRepositoryTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Test
    void testFindByCode() {
        // Мокируем ответ репозитория
        Currency mockCurrency = new Currency("USD", "United States Dollar", BigDecimal.valueOf(75.5));
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(mockCurrency));

        // Вызываем метод и проверяем результат
        Optional<Currency> found = currencyRepository.findByCode("USD");
        assertTrue(found.isPresent());
        assertEquals("USD", found.get().getCode());
        assertEquals(BigDecimal.valueOf(75.5), found.get().getExchangeRate());

        // Проверяем, что метод вызывался 1 раз
        verify(currencyRepository, times(1)).findByCode("USD");
    }

    @Test
    void testFindByCode_NotFound() {
        // Если валюты нет, должен вернуться пустой Optional
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());

        Optional<Currency> found = currencyRepository.findByCode("EUR");
        assertFalse(found.isPresent());

        verify(currencyRepository, times(1)).findByCode("EUR");
    }
}

