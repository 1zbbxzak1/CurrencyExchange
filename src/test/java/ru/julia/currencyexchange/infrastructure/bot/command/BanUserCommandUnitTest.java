package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.BanUserKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.BanUserCallbackHandler;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BanUserCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private UserService userService;
    @Mock
    private BanUserKeyboardBuilder keyboardBuilder;
    @Mock
    private BanUserCallbackHandler callbackHandler;
    private BanUserCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new BanUserCommand(messageConverter, userService, keyboardBuilder, callbackHandler);
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

    private User adminUser() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);
        Role adminRole = new Role("ROLE_ADMIN");
        UserRole userRole = new UserRole(user, adminRole);
        user.getRoles().add(userRole);
        return user;
    }

    private User regularUser() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);
        Role userRoleObj = new Role("ROLE_USER");
        UserRole userRole = new UserRole(user, userRoleObj);
        user.getRoles().add(userRole);
        return user;
    }

    @Test
    @DisplayName("Пользователь не найден (нет доступа)")
    void userNotFound_noAccess() {
        Update update = mockUpdate(1L);
        when(userService.findUserByChatId(1L)).thenReturn(null);
        when(messageConverter.resolve("command.banUser.no_access")).thenReturn("no_access");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_access");
    }

    @Test
    @DisplayName("Пользователь не админ (нет доступа)")
    void userNotAdmin_noAccess() {
        Update update = mockUpdate(2L);
        User user = regularUser();
        when(userService.findUserByChatId(2L)).thenReturn(user);
        when(messageConverter.resolve("command.banUser.no_access")).thenReturn("no_access");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_access");
    }

    @Test
    @DisplayName("Пользователь удалён или не верифицирован (нет доступа)")
    void userDeletedOrNotVerified_noAccess() {
        Update update = mockUpdate(3L);
        User user = adminUser();
        user.setDeleted(true);
        when(userService.findUserByChatId(3L)).thenReturn(user);
        when(messageConverter.resolve("command.banUser.no_access")).thenReturn("no_access");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_access");

        user.setDeleted(false);
        user.setVerified(false);
        when(userService.findUserByChatId(3L)).thenReturn(user);
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_access");
    }

    @Test
    @DisplayName("Нет пользователей для блокировки (users.isEmpty)")
    void noUsersToBan() {
        Update update = mockUpdate(4L);
        User admin = adminUser();
        when(userService.findUserByChatId(4L)).thenReturn(admin);
        when(userService.findAllUsers(null)).thenReturn(Collections.emptyList());
        when(messageConverter.resolve("command.banUser.empty")).thenReturn("empty");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("empty");
    }

    @Test
    @DisplayName("Есть пользователи для блокировки (корректная клавиатура и сообщение)")
    void usersToBan_keyboardAndMessage() {
        Update update = mockUpdate(5L);
        User admin = adminUser();
        when(userService.findUserByChatId(5L)).thenReturn(admin);
        User u1 = regularUser();
        u1.setEmail("a@a");
        List<User> users = List.of(u1);
        when(userService.findAllUsers(null)).thenReturn(users);
        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardBuilder.buildEmailKeyboard(users)).thenReturn(keyboard);
        when(messageConverter.resolve("command.banUser.select_email")).thenReturn("select_email");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("select_email");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
    }

    @Test
    @DisplayName("getCommand, getDescription, getCallbackHandler")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/banUser");
        assertThat(command.getDescription()).isEqualTo("command.banUser.description");
        assertThat(command.getCallbackHandler()).isEqualTo(callbackHandler);
    }

    @Test
    @DisplayName("isAccessible для разных ролей и состояний пользователя")
    void isAccessibleVariants() {
        User admin = adminUser();
        assertThat(command.isAccessible(admin)).isTrue();
        admin.setDeleted(true);
        assertThat(command.isAccessible(admin)).isFalse();
        admin.setDeleted(false);
        admin.setVerified(false);
        assertThat(command.isAccessible(admin)).isFalse();
        User user = regularUser();
        assertThat(command.isAccessible(user)).isFalse();
        assertThat(command.isAccessible(null)).isFalse();
    }
} 