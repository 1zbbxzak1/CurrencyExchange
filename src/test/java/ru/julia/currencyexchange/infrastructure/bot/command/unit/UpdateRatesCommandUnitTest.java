package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
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
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.UpdateRatesCommand;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateRatesCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private CurrencyExchangeService currencyExchangeService;
    @Mock
    private UserService userService;
    @InjectMocks
    private UpdateRatesCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(messageConverter.resolve(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(messageConverter.resolve(anyString(), anyMap())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Обычный вызов")
    void handle_admin_success() {
        Long chatId = 1L;
        String username = "admin";

        Update update = mockUpdate(chatId, username);

        User user = mockUser(true, false, false);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(chatId, username);

        List<Currency> currencies = List.of(mock(Currency.class), mock(Currency.class));
        when(currencyExchangeService.updateCurrencyRates(anyString())).thenReturn(currencies);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.updateRates.success");
    }

    private Update mockUpdate(Long chatId, String username) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);

        return update;
    }

    private User mockUser(boolean admin, boolean deleted, boolean banned) {
        User user = mock(User.class);

        when(user.isDeleted()).thenReturn(deleted);
        when(user.isBanned()).thenReturn(banned);

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

    @Test
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        Long chatId = 2L;

        Update update = mockUpdate(chatId, "user2");

        User user = mockUser(false, false, false);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.updateRates.error");
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        Long chatId = 3L;

        Update update = mockUpdate(chatId, "user3");

        User user = mockUser(true, true, false);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.updateRates.error");
    }

    @Test
    @DisplayName("Нет доступа: забанен")
    void handle_banned() {
        Long chatId = 4L;

        Update update = mockUpdate(chatId, "user4");

        User user = mockUser(true, false, true);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.updateRates.error");
    }

    @Test
    @DisplayName("Пользователь не зарегистрирован")
    void handle_notRegistered() {
        Long chatId = 5L;

        Update update = mockUpdate(chatId, "user5");

        when(userService.existsByChatId(chatId)).thenReturn(false);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.updateRates.error");
    }

    @Test
    @DisplayName("getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/updateRates");
        assertThat(command.getDescription()).isEqualTo("command.updateRates.description");
    }

    @Test
    @DisplayName("isAccessible: admin")
    void isAccessible_admin() {
        User user = mockUser(true, false, false);
        assertThat(command.isAccessible(user)).isTrue();
    }

    @Test
    @DisplayName("isAccessible: не admin")
    void isAccessible_notAdmin() {
        User user = mockUser(false, false, false);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: удалён")
    void isAccessible_deleted() {
        User user = mockUser(true, true, false);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: забанен")
    void isAccessible_banned() {
        User user = mockUser(true, false, true);
        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("isAccessible: null")
    void isAccessible_null() {
        assertThat(command.isAccessible(null)).isFalse();
    }
} 