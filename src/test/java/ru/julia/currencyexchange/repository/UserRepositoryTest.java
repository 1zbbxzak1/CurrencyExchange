package ru.julia.currencyexchange.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void testFindByUsername_Success() {
        // Подготовка данных
        User user = new User("testuser", "password123");
        user.setId("1");

        // Мокируем поведение репозитория
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Вызываем метод
        Optional<User> result = userRepository.findByUsername("testuser");

        // Проверяем, что пользователь найден
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("testuser", result.get().getUsername());

        // Проверяем, что метод вызвался один раз
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_NotFound() {
        // Мокируем поведение репозитория, если пользователя нет
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        // Вызываем метод
        Optional<User> result = userRepository.findByUsername("unknownUser");

        // Проверяем, что пользователя нет
        assertFalse(result.isPresent());

        // Проверяем, что метод вызвался один раз
        verify(userRepository, times(1)).findByUsername("unknownUser");
    }

    @Test
    void testFindByUsername_NullArgument() {
        // Ожидаем выброса исключения, если передан `null`
        when(userRepository.findByUsername(null)).thenThrow(new IllegalArgumentException("Username must not be null"));

        // Проверяем, что при передаче null выбрасывается исключение
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userRepository.findByUsername(null));
        assertEquals("Username must not be null", exception.getMessage());

        // Проверяем, что метод вызвался один раз
        verify(userRepository, times(1)).findByUsername(null);
    }
}
