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
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.HistoryCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.HistoryCallbackHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HistoryCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private CurrencyExchangeService currencyExchangeService;
    @Mock
    private UserService userService;
    @Mock
    private HistoryMessageBuilder historyMessageBuilder;
    @Mock
    private PaginationKeyboardBuilder paginationKeyboardBuilder;
    @Mock
    private HistoryCallbackHandler historyCallbackHandler;
    @InjectMocks
    private HistoryCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(messageConverter.resolve(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Есть конвертации")
    void handle_success() {
        Long chatId = 1L;
        String username = "user";
        Update update = mockUpdate(chatId, username);

        User user = mockUser("u1", false, false, true, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        doNothing().when(userService).updateUsernameIfChanged(chatId, username);

        CurrencyConversion conv = mock(CurrencyConversion.class);
        when(currencyExchangeService.getUserHistory("u1")).thenReturn(List.of(conv));
        when(historyMessageBuilder.buildHistoryMessage(anyList(), anyInt(), anyBoolean(), anyInt())).thenReturn("MSG");

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(paginationKeyboardBuilder.buildHistoryPaginationKeyboard(anyInt(), anyInt(), anyInt())).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("MSG");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
    }

    private Update mockUpdate(Long chatId, String username) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);
        when(message.text()).thenReturn("/history");

        return update;
    }

    private User mockUser(String id, boolean banned, boolean deleted, boolean verified, Set<String> roles) {
        User user = mock(User.class);

        when(user.getId()).thenReturn(id);
        when(user.isBanned()).thenReturn(banned);
        when(user.isDeleted()).thenReturn(deleted);
        when(user.isVerified()).thenReturn(verified);

        if (roles != null && !roles.isEmpty()) {
            Set<UserRole> userRoles = new HashSet<>();
            for (String roleName : roles) {
                Role role = mock(Role.class);
                when(role.getRoleName()).thenReturn(roleName);
                UserRole userRole = mock(UserRole.class);
                when(userRole.getRole()).thenReturn(role);
                userRoles.add(userRole);
            }
            when(user.getRoles()).thenReturn(userRoles);
        } else {
            when(user.getRoles()).thenReturn(emptySet());
        }

        return user;
    }

    @Test
    @DisplayName("Пользователь не зарегистрирован")
    void handle_notRegistered() {
        Long chatId = 2L;
        Update update = mockUpdate(chatId, "user2");
        when(userService.existsByChatId(chatId)).thenReturn(false);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.error");
    }

    @Test
    @DisplayName("Пользователь забанен")
    void handle_banned() {
        Long chatId = 3L;

        Update update = mockUpdate(chatId, "user3");

        User user = mockUser("u3", true, false, true, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.error");
    }

    @Test
    @DisplayName("Пользователь удалён")
    void handle_deleted() {
        Long chatId = 4L;

        Update update = mockUpdate(chatId, "user4");

        User user = mockUser("u4", false, true, true, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.error");
    }

    @Test
    @DisplayName("Пользователь не верифицирован")
    void handle_notVerified() {
        Long chatId = 5L;

        Update update = mockUpdate(chatId, "user5");

        User user = mockUser("u5", false, false, false, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.error");
    }

    @Test
    @DisplayName("Нет конвертаций")
    void handle_noConversions() {
        Long chatId = 6L;

        Update update = mockUpdate(chatId, "user6");

        User user = mockUser("u6", false, false, true, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(chatId, "user6");

        when(currencyExchangeService.getUserHistory("u6")).thenReturn(Collections.emptyList());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.no_conversions");
    }

    @Test
    @DisplayName("Ошибка сервиса")
    void handle_serviceError() {
        Long chatId = 7L;

        Update update = mockUpdate(chatId, "user7");

        User user = mockUser("u7", false, false, true, Set.of("USER"));
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(chatId, "user7");

        when(currencyExchangeService.getUserHistory("u7")).thenThrow(new RuntimeException("fail"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.history.error");
    }

    @Test
    @DisplayName("getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/history");
        assertThat(command.getDescription()).isEqualTo("command.history.description");
    }

    @Test
    @DisplayName("isAccessible: USER")
    void isAccessible_user() {
        User user = mockUser("u8", false, false, true, Set.of("USER"));
        assertThat(command.isAccessible(user)).isTrue();
    }

    @Test
    @DisplayName("isAccessible: ADMIN")
    void isAccessible_admin() {
        User user = mockUser("u9", false, false, true, Set.of("ADMIN"));
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: null")
    void isAccessible_null() {
        assertThat(command.isAccessible(null)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: удалён")
    void isAccessible_deleted() {
        User user = mockUser("u10", false, true, true, Set.of("USER"));
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: не верифицирован")
    void isAccessible_notVerified() {
        User user = mockUser("u11", false, false, false, Set.of("USER"));
        assertThat(command.isAccessible(user)).isFalse();
    }
}