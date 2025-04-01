package ru.julia.currencyexchange.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.entity.Role;
import ru.julia.currencyexchange.entity.enums.RoleEnum;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryTest {

    @Mock
    private RoleRepository roleRepository;

    @Test
    void testFindByRoleName_Success() {
        // Подготовка данных
        Role adminRole = new Role();
        adminRole.setId("1");
        adminRole.setRoleName(RoleEnum.ADMIN);

        // Мокируем поведение репозитория
        when(roleRepository.findByRoleName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));

        // Вызываем метод
        Optional<Role> result = roleRepository.findByRoleName(RoleEnum.ADMIN);

        // Проверяем, что роль найдена
        assertTrue(result.isPresent());
        assertEquals(RoleEnum.ADMIN.getRoleName(), result.get().getRoleName());

        // Проверяем, что метод вызвался один раз
        verify(roleRepository, times(1)).findByRoleName(RoleEnum.ADMIN);
    }

    @Test
    void testFindByRoleName_NotFound() {
        // Мокируем поведение репозитория для случая, когда роль не найдена
        when(roleRepository.findByRoleName(RoleEnum.USER)).thenReturn(Optional.empty());

        // Вызываем метод
        Optional<Role> result = roleRepository.findByRoleName(RoleEnum.USER);

        // Проверяем, что роль не найдена
        assertFalse(result.isPresent());

        // Проверяем, что метод вызвался один раз
        verify(roleRepository, times(1)).findByRoleName(RoleEnum.USER);
    }

    @Test
    void testFindByRoleName_NullArgument() {
        // Ожидаем выброса исключения, если передан `null`
        when(roleRepository.findByRoleName(null)).thenThrow(new IllegalArgumentException("RoleEnum must not be null"));

        // Проверяем, что при передаче null выбрасывается исключение
        Exception exception = assertThrows(IllegalArgumentException.class, () -> roleRepository.findByRoleName(null));
        assertEquals("RoleEnum must not be null", exception.getMessage());

        // Проверяем, что метод вызвался один раз
        verify(roleRepository, times(1)).findByRoleName(null);
    }
}

