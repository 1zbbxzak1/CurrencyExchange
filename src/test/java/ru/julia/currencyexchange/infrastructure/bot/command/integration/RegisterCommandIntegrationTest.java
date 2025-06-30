package ru.julia.currencyexchange.infrastructure.bot.command.integration;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.RegisterCommand;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RegisterCommandIntegrationTest {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationStateService registrationStateService;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    private RegisterCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterCommand(messageConverter, authService, userService, registrationStateService);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Успешная регистрация и верификация")
    void fullRegistrationFlow() {
        Long chatId = 100L;
        String username = "user100";
        Update update = mockUpdate(chatId, username, "/register");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.email_prompt"));
        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_EMAIL);

        update = mockUpdate(chatId, username, "test100@mail.com");
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.password_prompt"));
        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_PASSWORD);

        update = mockUpdate(chatId, username, "123456");
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.code_sent"));
        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_VERIFICATION_CODE);

        String code = userService.findUserByChatId(chatId).getVerificationCode();
        update = mockUpdate(chatId, username, code);
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.success"));
        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.NONE);

        User user = userService.findUserByChatId(chatId);
        assertThat(user.isVerified()).isTrue();
    }

    private Update mockUpdate(Long chatId, String username, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);
        when(message.text()).thenReturn(text);

        return update;
    }

    @Test
    @DisplayName("Невалидный email")
    void invalidEmail() {
        Long chatId = 101L;
        Update update = mockUpdate(chatId, "user101", "/register");
        command.handle(update);

        update = mockUpdate(chatId, "user101", "bademail");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.invalid_email"));

        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_EMAIL);
    }

    @Test
    @DisplayName("Короткий пароль")
    void shortPassword() {
        Long chatId = 102L;
        Update update = mockUpdate(chatId, "user102", "/register");
        command.handle(update);

        update = mockUpdate(chatId, "user102", "test102@mail.com");
        command.handle(update);

        update = mockUpdate(chatId, "user102", "123");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.password_too_short"));

        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_PASSWORD);
    }

    @Test
    @DisplayName("Пользователь уже существует")
    void userAlreadyExists() {
        Long chatId = 103L;
        String username = "user103";
        authService.createUserWithVerificationCode(chatId, username, "test103@mail.com", "123456");

        Update update = mockUpdate(chatId, username, "/register");
        command.handle(update);

        update = mockUpdate(chatId, username, "test103@mail.com");
        command.handle(update);

        update = mockUpdate(chatId, username, "123456");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.register.error"));

        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.NONE);
    }

    @Test
    @DisplayName("Неверный код верификации")
    void invalidVerificationCode() {
        Long chatId = 104L;
        String username = "user104";

        Update update = mockUpdate(chatId, username, "/register");
        command.handle(update);

        update = mockUpdate(chatId, username, "test104@mail.com");
        command.handle(update);

        update = mockUpdate(chatId, username, "123456");
        command.handle(update);

        update = mockUpdate(chatId, username, "wrongcode");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.invalid_code"));

        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_VERIFICATION_CODE);
    }

    @Test
    @DisplayName("Ошибка при верификации (исключение)")
    void errorOnVerification() {
        Long chatId = 105L;
        String username = "user105";

        Update update = mockUpdate(chatId, username, "/register");
        command.handle(update);

        update = mockUpdate(chatId, username, "test105@mail.com");
        command.handle(update);

        update = mockUpdate(chatId, username, "123456");
        command.handle(update);

        registrationStateService.getData(chatId).setEmail(null);
        update = mockUpdate(chatId, username, "anycode");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.register.invalid_code"));

        assertThat(registrationStateService.getState(chatId)).isEqualTo(RegistrationState.WAITING_VERIFICATION_CODE);
    }

    @Test
    @DisplayName("matches, getCommand, getDescription")
    void metaMethods() {
        Long chatId = 106L;

        Update update = mockUpdate(chatId, "user106", "/register");
        assertThat(command.matches(update)).isTrue();

        command.handle(update);

        Update update2 = mockUpdate(chatId, "user106", "test106@mail.com");
        assertThat(command.matches(update2)).isTrue();

        Update update3 = mock(Update.class);
        when(update3.message()).thenReturn(null);

        assertThat(command.matches(update3)).isFalse();
        assertThat(command.getCommand()).isEqualTo("/register");
        assertThat(command.getDescription()).isEqualTo("command.register.description");
    }
} 