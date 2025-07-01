package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.UsersCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.UserMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.UsersCallbackHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsersCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private UserService userService;
    @Mock
    private UserMessageBuilder userMessageBuilder;
    @Mock
    private PaginationKeyboardBuilder paginationKeyboardBuilder;
    @Mock
    private UsersCallbackHandler usersCallbackHandler;
    @InjectMocks
    private UsersCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(messageConverter.resolve(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(messageConverter.resolve(anyString(), anyMap())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Есть пользователи")
    void handle_admin_success() {
        Long chatId = 1L;
        User admin = mockUser(true, false, true);

        Update update = mockUpdate(chatId);

        when(userService.findUserByChatId(chatId)).thenReturn(admin);

        List<User> users = List.of(mockUser(false, false, true), mockUser(false, false, true));
        when(userService.findAllUsers(null)).thenReturn(users);
        when(userMessageBuilder.buildUsersMessage(anyList(), anyInt(), anyBoolean(), anyInt())).thenReturn("USERS");

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(paginationKeyboardBuilder.buildUsersPaginationKeyboard(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("USERS");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
    }

    private User mockUser(boolean admin, boolean deleted, boolean verified) {
        User user = mock(User.class);
        when(user.isDeleted()).thenReturn(deleted);
        when(user.isVerified()).thenReturn(verified);
        Set<UserRole> roles = new HashSet<>();

        if (admin) {
            Role role = mock(Role.class);
            when(role.getRoleName()).thenReturn("ADMIN");
            UserRole userRole = mock(UserRole.class);
            when(userRole.getRole()).thenReturn(role);
            roles.add(userRole);
        }

        when(user.getRoles()).thenReturn(roles);

        return user;
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
    @DisplayName("Нет пользователей")
    void handle_noUsers() {
        Long chatId = 2L;
        User admin = mockUser(true, false, true);

        Update update = mockUpdate(chatId);

        when(userService.findUserByChatId(chatId)).thenReturn(admin);
        when(userService.findAllUsers(null)).thenReturn(Collections.emptyList());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.users.empty");
    }

    @Test
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        Long chatId = 3L;
        User user = mockUser(false, false, true);

        Update update = mockUpdate(chatId);

        when(userService.findUserByChatId(chatId)).thenReturn(user);
        
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.users.no_access");
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        Long chatId = 4L;
        User user = mockUser(true, true, true);
        Update update = mockUpdate(chatId);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.users.no_access");
    }

    @Test
    @DisplayName("Нет доступа: не верифицирован")
    void handle_notVerified() {
        Long chatId = 5L;
        User user = mockUser(true, false, false);
        Update update = mockUpdate(chatId);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.users.no_access");
    }

    @Test
    @DisplayName("getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/users");
        assertThat(command.getDescription()).isEqualTo("command.users.description");
    }

    @Test
    @DisplayName("isAccessible: admin")
    void isAccessible_admin() {
        User user = mockUser(true, false, true);
        assertThat(command.isAccessible(user)).isTrue();
    }

    @Test
    @DisplayName("isAccessible: не admin")
    void isAccessible_notAdmin() {
        User user = mockUser(false, false, true);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: удалён")
    void isAccessible_deleted() {
        User user = mockUser(true, true, true);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: не верифицирован")
    void isAccessible_notVerified() {
        User user = mockUser(true, false, false);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: null")
    void isAccessible_null() {
        assertThat(command.isAccessible(null)).isFalse();
    }
} 