package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.HelpCommand;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private UserService userService;
    @InjectMocks
    private HelpCommand command;

    @Test
    @DisplayName("Не зарегистрирован")
    void handle_notRegistered() {
        Long chatId = 1L;

        Update update = mockUpdate(chatId);

        when(userService.existsByChatId(chatId)).thenReturn(false);
        when(messageConverter.resolve("command.help.unregistered_help_message")).thenReturn("UNREG");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("UNREG");
    }

    private Update mockUpdate(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }

    @Test
    @DisplayName("Забанен")
    void handle_banned() {
        Long chatId = 2L;

        Update update = mockUpdate(chatId);

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(true);
        when(messageConverter.resolve("command.help.banned_message")).thenReturn("BANNED");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("BANNED");
    }

    @Test
    @DisplayName("Удалён")
    void handle_deleted() {
        Long chatId = 3L;

        Update update = mockUpdate(chatId);

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(true);
        when(messageConverter.resolve("command.help.deleted_help_message")).thenReturn("DELETED");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("Верифицированный admin")
    void handle_admin() {
        Long chatId = 4L;

        Update update = mockUpdate(chatId);

        User user = mock(User.class);
        Role adminRole = new Role("ROLE_ADMIN");
        UserRole userRole = new UserRole(user, adminRole);

        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(userRole));
        when(messageConverter.resolve("command.help.admin_help_message")).thenReturn("ADMIN");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Верифицированный user")
    void handle_user() {
        Long chatId = 5L;

        Update update = mockUpdate(chatId);

        User user = mock(User.class);
        Role userRoleObj = new Role("ROLE_USER");
        UserRole userRole = new UserRole(user, userRoleObj);

        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(userRole));
        when(messageConverter.resolve("command.help.user_help_message")).thenReturn("USER");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("USER");
    }

    @Test
    @DisplayName("Не верифицирован")
    void handle_unverified() {
        Long chatId = 6L;

        Update update = mockUpdate(chatId);

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(false);
        when(messageConverter.resolve("command.help.unverified_help_message")).thenReturn("UNVERIFIED");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("UNVERIFIED");
    }

    @Test
    @DisplayName("Ошибка")
    void handle_error() {
        Long chatId = 7L;

        Update update = mockUpdate(chatId);

        when(userService.existsByChatId(chatId)).thenThrow(new RuntimeException("fail"));
        when(messageConverter.resolve("command.help.error_message")).thenReturn("ERROR");
        
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Проверка getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/help");
        assertThat(command.getDescription()).isEqualTo("command.help.description");
    }
} 