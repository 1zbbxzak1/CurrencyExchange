package ru.julia.currencyexchange.application.service.bot.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.domain.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DeleteAccountValidationServiceUnitTest {
    @Mock
    private UserService userService;
    @Mock
    private MessageConverter messageConverter;
    private DeleteAccountValidationService service;

    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new DeleteAccountValidationService(userService, messageConverter);
    }

    @Test
    @DisplayName("Пользователь не найден (not_registered)")
    void userNotFound() {
        when(userService.existsByChatId(1L)).thenReturn(false);
        when(messageConverter.resolve("command.deleteAccount.not_registered")).thenReturn("not_registered");

        ValidationResult result = service.validateUserForDeletion(1L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("not_registered");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("Пользователь найден, не забанен, не удалён (успех)")
    void userFoundSuccess() {
        User user = new User();
        user.setBanned(false);
        user.setDeleted(false);

        when(userService.existsByChatId(2L)).thenReturn(true);
        when(userService.findUserByChatId(2L)).thenReturn(user);

        ValidationResult result = service.validateUserForDeletion(2L);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Пользователь забанен (banned)")
    void userBanned() {
        User user = new User();
        user.setBanned(true);
        user.setDeleted(false);

        when(userService.existsByChatId(3L)).thenReturn(true);
        when(userService.findUserByChatId(3L)).thenReturn(user);
        when(messageConverter.resolve("command.deleteAccount.banned")).thenReturn("banned");

        ValidationResult result = service.validateUserForDeletion(3L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("banned");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("Пользователь удалён (already_deleted)")
    void userDeleted() {
        User user = new User();
        user.setBanned(false);
        user.setDeleted(true);

        when(userService.existsByChatId(4L)).thenReturn(true);
        when(userService.findUserByChatId(4L)).thenReturn(user);
        when(messageConverter.resolve("command.deleteAccount.already_deleted")).thenReturn("already_deleted");

        ValidationResult result = service.validateUserForDeletion(4L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("already_deleted");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("UserService кидает исключение (error)")
    void userServiceThrows() {
        when(userService.existsByChatId(5L)).thenThrow(new RuntimeException("fail"));
        when(messageConverter.resolve("command.deleteAccount.error")).thenReturn("error");

        ValidationResult result = service.validateUserForDeletion(5L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("error");
        assertThat(result.getUser()).isNull();
    }
} 