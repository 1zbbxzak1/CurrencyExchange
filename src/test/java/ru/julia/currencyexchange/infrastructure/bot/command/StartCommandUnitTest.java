package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class StartCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private UserService userService;
    private StartCommand command;

    @BeforeEach
    void setUp() {
        openMocks(this);
        command = new StartCommand(messageConverter, userService);
    }

    @Test
    @DisplayName("Новый пользователь")
    void newUser() {
        Update update = mockUpdate(1L);

        when(userService.existsByChatId(1L)).thenReturn(false);
        when(messageConverter.resolve(eq("command.start.start_message"), anyMap())).thenReturn("start");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("start");
    }

    private Update mockUpdate(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn("user");
        when(chat.firstName()).thenReturn("Имя");

        return update;
    }

    @Test
    @DisplayName("Верифицированный пользователь")
    void verifiedUser() {
        Update update = mockUpdate(2L);

        when(userService.existsByChatId(2L)).thenReturn(true);

        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);
        user.setBanned(false);

        when(userService.findUserByChatId(2L)).thenReturn(user);
        when(messageConverter.resolve(eq("command.start.welcome_back_message"), anyMap())).thenReturn("welcome");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("welcome");
    }

    @Test
    @DisplayName("Пользователь не верифицирован")
    void notVerifiedUser() {
        Update update = mockUpdate(3L);

        when(userService.existsByChatId(3L)).thenReturn(true);

        User user = new User();
        user.setVerified(false);
        user.setDeleted(false);
        user.setBanned(false);

        when(userService.findUserByChatId(3L)).thenReturn(user);
        when(messageConverter.resolve("command.start.not_verified_message")).thenReturn("not_verified");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("not_verified");
    }

    @Test
    @DisplayName("Пользователь забанен")
    void bannedUser() {
        Update update = mockUpdate(4L);

        when(userService.existsByChatId(4L)).thenReturn(true);

        User user = new User();
        user.setBanned(true);
        user.setDeleted(false);
        user.setVerified(true);

        when(userService.findUserByChatId(4L)).thenReturn(user);
        when(messageConverter.resolve("command.start.banned_message")).thenReturn("banned");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("banned");
    }

    @Test
    @DisplayName("Пользователь удалён")
    void deletedUser() {
        Update update = mockUpdate(5L);

        when(userService.existsByChatId(5L)).thenReturn(true);

        User user = new User();
        user.setDeleted(true);
        user.setBanned(false);
        user.setVerified(true);

        when(userService.findUserByChatId(5L)).thenReturn(user);
        when(messageConverter.resolve("command.start.deleted_message")).thenReturn("deleted");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("deleted");
    }

    @Test
    @DisplayName("Ошибка в сервисе")
    void serviceException() {
        Update update = mockUpdate(6L);
        
        when(userService.existsByChatId(6L)).thenThrow(new RuntimeException());
        when(messageConverter.resolve("command.start.error_message")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }

    @Test
    void getCommandAndDescription() {
        assertThat(command.getCommand()).isEqualTo("/start");
        assertThat(command.getDescription()).isEqualTo("command.start.description");
    }
}
