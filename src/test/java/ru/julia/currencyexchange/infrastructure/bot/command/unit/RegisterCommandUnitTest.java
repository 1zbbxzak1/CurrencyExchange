package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.RegistrationData;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.infrastructure.bot.command.RegisterCommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class RegisterCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private RegistrationStateService registrationStateService;
    private RegisterCommand command;

    @BeforeEach
    void setUp() {
        openMocks(this);
        command = new RegisterCommand(messageConverter, authService, userService, registrationStateService);
    }

    @Test
    @DisplayName("/register: старт регистрации")
    void startRegistration() {
        Update update = mockUpdate(1L, "/register");
        when(messageConverter.resolve("command.register.email_prompt")).thenReturn("email_prompt");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("email_prompt");

        verify(registrationStateService).setState(1L, RegistrationState.WAITING_EMAIL);
    }

    private Update mockUpdate(Long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn("user");
        when(message.text()).thenReturn(text);

        return update;
    }

    @Test
    @DisplayName("Ввод невалидного email")
    void invalidEmail() {
        Update update = mockUpdate(2L, "bademail");

        when(registrationStateService.getState(2L)).thenReturn(RegistrationState.WAITING_EMAIL);
        when(messageConverter.resolve("command.register.invalid_email")).thenReturn("invalid_email");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("invalid_email");
    }

    @Test
    @DisplayName("Ввод валидного email")
    void validEmail() {
        Update update = mockUpdate(3L, "test@mail.com");

        when(registrationStateService.getState(3L)).thenReturn(RegistrationState.WAITING_EMAIL);
        when(messageConverter.resolve("command.register.password_prompt")).thenReturn("password_prompt");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("password_prompt");

        verify(registrationStateService).setEmail(3L, "test@mail.com");
        verify(registrationStateService).setState(3L, RegistrationState.WAITING_PASSWORD);
    }

    @Test
    @DisplayName("Короткий пароль")
    void shortPassword() {
        Update update = mockUpdate(4L, "123");
        when(registrationStateService.getState(4L)).thenReturn(RegistrationState.WAITING_PASSWORD);
        when(messageConverter.resolve("command.register.password_too_short")).thenReturn("too_short");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("too_short");
    }

    @Test
    @DisplayName("Пользователь уже существует")
    void userAlreadyExists() {
        Update update = mockUpdate(5L, "123456");

        when(registrationStateService.getState(5L)).thenReturn(RegistrationState.WAITING_PASSWORD);
        when(userService.existsActiveUserByChatId(5L)).thenReturn(true);
        when(messageConverter.resolve("command.register.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
        verify(registrationStateService).clearData(5L);
    }

    @Test
    @DisplayName("Успешная регистрация (пароль)")
    void successfulPassword() {
        Update update = mockUpdate(6L, "123456");
        when(registrationStateService.getState(6L)).thenReturn(RegistrationState.WAITING_PASSWORD);
        when(userService.existsActiveUserByChatId(6L)).thenReturn(false);

        RegistrationData data = new RegistrationData();
        data.setEmail("test@mail.com");

        when(registrationStateService.getData(6L)).thenReturn(data);
        when(messageConverter.resolve("command.register.code_sent")).thenReturn("code_sent");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("code_sent");
        verify(registrationStateService).setState(6L, RegistrationState.WAITING_VERIFICATION_CODE);
    }

    @Test
    @DisplayName("Ошибка при создании пользователя")
    void errorOnCreateUser() {
        Update update = mockUpdate(7L, "123456");
        when(registrationStateService.getState(7L)).thenReturn(RegistrationState.WAITING_PASSWORD);
        when(userService.existsActiveUserByChatId(7L)).thenReturn(false);

        RegistrationData data = new RegistrationData();
        data.setEmail("test@mail.com");

        when(registrationStateService.getData(7L)).thenReturn(data);
        doThrow(new RuntimeException("fail")).when(authService).createUserWithVerificationCode(anyLong(), anyString(), anyString(), anyString());
        when(messageConverter.resolve("command.register.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("error");
        verify(registrationStateService).clearData(7L);
    }

    @Test
    @DisplayName("Успешная верификация")
    void successfulVerification() {
        Update update = mockUpdate(8L, "code");
        when(registrationStateService.getState(8L)).thenReturn(RegistrationState.WAITING_VERIFICATION_CODE);

        RegistrationData data = new RegistrationData();
        data.setEmail("test@mail.com");

        when(registrationStateService.getData(8L)).thenReturn(data);
        when(userService.verifyUserCode(8L, "test@mail.com", "code")).thenReturn(true);
        when(messageConverter.resolve("command.register.success")).thenReturn("success");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("success");
        verify(registrationStateService).clearData(8L);
    }

    @Test
    @DisplayName("Неверный код верификации")
    void invalidVerificationCode() {
        Update update = mockUpdate(9L, "badcode");
        when(registrationStateService.getState(9L)).thenReturn(RegistrationState.WAITING_VERIFICATION_CODE);

        RegistrationData data = new RegistrationData();
        data.setEmail("test@mail.com");

        when(registrationStateService.getData(9L)).thenReturn(data);
        when(userService.verifyUserCode(9L, "test@mail.com", "badcode")).thenReturn(false);
        when(messageConverter.resolve("command.register.invalid_code")).thenReturn("invalid_code");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("invalid_code");
    }

    @Test
    @DisplayName("Ошибка при верификации")
    void errorOnVerification() {
        Update update = mockUpdate(10L, "badcode");
        when(registrationStateService.getState(10L)).thenReturn(RegistrationState.WAITING_VERIFICATION_CODE);

        RegistrationData data = new RegistrationData();
        data.setEmail("test@mail.com");

        when(registrationStateService.getData(10L)).thenReturn(data);
        when(userService.verifyUserCode(10L, "test@mail.com", "badcode")).thenThrow(new RuntimeException());
        when(messageConverter.resolve("command.register.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
        verify(registrationStateService).clearData(10L);
    }

    @Test
    @DisplayName("matches, getCommand, getDescription")
    void metaMethods() {
        Update update = mockUpdate(11L, "/register");

        when(registrationStateService.getState(11L)).thenReturn(RegistrationState.NONE);
        assertThat(command.matches(update)).isTrue();

        Update update2 = mockUpdate(12L, "test");
        when(registrationStateService.getState(12L)).thenReturn(RegistrationState.WAITING_EMAIL);
        assertThat(command.matches(update2)).isTrue();

        Update update3 = mock(Update.class);
        when(update3.message()).thenReturn(null);

        assertThat(command.matches(update3)).isFalse();
        assertThat(command.getCommand()).isEqualTo("/register");
        assertThat(command.getDescription()).isEqualTo("command.register.description");
    }
}
