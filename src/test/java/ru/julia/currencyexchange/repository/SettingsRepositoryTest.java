package ru.julia.currencyexchange.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.entity.Settings;
import ru.julia.currencyexchange.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsRepositoryTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Test
    void testFindByUserId_Success() {
        // Подготовка данных
        User user = new User();
        user.setId("123");

        Currency currency = new Currency("USD", "Dollar", null);
        Settings settings = new Settings(user, currency);

        // Мокируем поведение репозитория
        when(settingsRepository.findByUserId("123")).thenReturn(Optional.of(settings));

        // Вызываем метод
        Optional<Settings> result = settingsRepository.findByUserId("123");

        // Проверяем, что настройки найдены
        assertTrue(result.isPresent());
        assertEquals("123", result.get().getUser().getId());
        assertEquals("USD", result.get().getPreferredCurrency().getCode());

        // Проверяем, что метод вызвался один раз
        verify(settingsRepository, times(1)).findByUserId("123");
    }

    @Test
    void testFindByUserId_NotFound() {
        // Мокируем поведение репозитория для случая, когда настройки не найдены
        when(settingsRepository.findByUserId("999")).thenReturn(Optional.empty());

        // Вызываем метод
        Optional<Settings> result = settingsRepository.findByUserId("999");

        // Проверяем, что настройки не найдены
        assertFalse(result.isPresent());

        // Проверяем, что метод вызвался один раз
        verify(settingsRepository, times(1)).findByUserId("999");
    }

    @Test
    void testFindByUserId_NullArgument() {
        // Ожидаем выброса исключения, если передан `null`
        when(settingsRepository.findByUserId(null)).thenThrow(new IllegalArgumentException("User ID must not be null"));

        // Проверяем, что при передаче null выбрасывается исключение
        Exception exception = assertThrows(IllegalArgumentException.class, () -> settingsRepository.findByUserId(null));
        assertEquals("User ID must not be null", exception.getMessage());

        // Проверяем, что метод вызвался один раз
        verify(settingsRepository, times(1)).findByUserId(null);
    }
}


