package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Успешный поиск пользователя по chatId")
    void findUserByChatId_success() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(1L);
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(user));
        User result = userService.findUserByChatId(1L);
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Пользователь не найден по chatId")
    void findUserByChatId_notFound() {
        when(userRepository.findByChatId(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserByChatId(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("Пользователь существует по chatId")
    void existsByChatId_true() {
        when(userRepository.existsByChatId(1L)).thenReturn(true);
        assertThat(userService.existsByChatId(1L)).isTrue();
    }

    @Test
    @DisplayName("Пользователь не существует по chatId")
    void existsByChatId_false() {
        when(userRepository.existsByChatId(2L)).thenReturn(false);
        assertThat(userService.existsByChatId(2L)).isFalse();
    }

    @Test
    @DisplayName("Успешное удаление пользователя по id")
    void deleteUserById_success() {
        User user = new User("test@mail.com", "pass");
        setUserId(user, "id1");
        when(userRepository.findById("id1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        User deleted = userService.deleteUserById("id1");
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.isVerified()).isFalse();
        verify(userRepository).save(user);
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
    @DisplayName("Пользователь с id не найден для удаления")
    void deleteUserById_notFound() {
        when(userRepository.findById("id2")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.deleteUserById("id2"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id2");
    }

    @Test
    @DisplayName("Успешное мягкое удаление пользователя по chatId")
    void softDeleteUserByChatId_success() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(1L);
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(user));
        userService.softDeleteUserByChatId(1L);
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.isVerified()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Пользователь с chatId не найден для мягкого удаления")
    void softDeleteUserByChatId_notFound() {
        when(userRepository.findByChatId(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.softDeleteUserByChatId(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("Пользователь активен по chatId")
    void existsActiveUserByChatId_true() {
        when(userRepository.existsActiveByChatId(1L)).thenReturn(true);
        assertThat(userService.existsActiveUserByChatId(1L)).isTrue();
    }

    @Test
    @DisplayName("Пользователь не активен по chatId")
    void existsActiveUserByChatId_false() {
        when(userRepository.existsActiveByChatId(2L)).thenReturn(false);
        assertThat(userService.existsActiveUserByChatId(2L)).isFalse();
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAllUsers_returnsAll() {
        User user1 = new User("a@mail.com", "p1");
        setUserId(user1, "1");
        User user2 = new User("b@mail.com", "p2");
        setUserId(user2, "2");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<User> users = userService.findAllUsers(null);
        assertThat(users).containsExactly(user1, user2);
    }

    @Test
    @DisplayName("Успешная верификация пользователя по коду")
    void verifyUserCode_success() {
        User user = new User("test@mail.com", "pass");
        user.setVerificationCode("1234");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        boolean result = userService.verifyUserCode(1L, "test@mail.com", "1234");
        assertThat(result).isTrue();
        assertThat(user.isVerified()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Верификация пользователя по коду не удалась")
    void verifyUserCode_wrongCode() {
        User user = new User("test@mail.com", "pass");
        user.setVerificationCode("1234");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        boolean result = userService.verifyUserCode(1L, "test@mail.com", "0000");
        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное получение id пользователя по chatId")
    void getUserIdByChatId_success() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(1L);
        setUserId(user, "id1");
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(user));
        String id = userService.getUserIdByChatId(1L);
        assertThat(id).isEqualTo("id1");
    }

    @Test
    @DisplayName("Пользователь с chatId не найден для получения id")
    void getUserIdByChatId_notFound() {
        when(userRepository.findByChatId(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserIdByChatId(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("Имя пользователя обновлено, так как изменилось")
    void updateUsernameIfChanged_changed() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(1L);
        user.setUsername("old");
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(user));
        userService.updateUsernameIfChanged(1L, "new");
        assertThat(user.getUsername()).isEqualTo("new");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Имя пользователя не обновлено, так как не изменилось")
    void updateUsernameIfChanged_notChanged() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(1L);
        user.setUsername("same");
        when(userRepository.findByChatId(1L)).thenReturn(Optional.of(user));
        userService.updateUsernameIfChanged(1L, "same");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешный поиск пользователя по email")
    void findUserByEmail_success() {
        User user = new User("test@mail.com", "pass");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        User result = userService.findUserByEmail("test@mail.com");
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Пользователь не найден по email")
    void findUserByEmail_notFound() {
        when(userRepository.findByEmail("none@mail.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserByEmail("none@mail.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("none@mail.com");
    }

    @Test
    @DisplayName("Сохранение пользователя делегируется репозиторию")
    void saveUser_delegates() {
        User user = new User("test@mail.com", "pass");
        when(userRepository.save(user)).thenReturn(user);
        User result = userService.saveUser(user);
        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }
} 