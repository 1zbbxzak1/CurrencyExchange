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
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.SetFeeStateService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.SetFeeCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.SetFeeKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.SetFeeCallbackHandler;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SetFeeCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private UserService userService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private SetFeeStateService setFeeStateService;
    @Mock
    private SetFeeKeyboardBuilder keyboardBuilder;
    @Mock
    private SetFeeCallbackHandler callbackHandler;
    @InjectMocks
    private SetFeeCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(messageConverter.resolve(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(messageConverter.resolve(anyString(), anyMap())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Обычный вызов")
    void handle_admin_success() {
        Long chatId = 1L;
        User user = mockUser(true, false, true);

        Update update = mockUpdate(chatId, "/setFee");
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(setFeeStateService.getState(chatId)).thenReturn(SetFeeState.NONE);
        when(settingsService.getGlobalConversionFeePercent()).thenReturn(2.5);

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardBuilder.buildFeeSelectionKeyboard(2.5)).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.current_fee");
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

    private Update mockUpdate(Long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(text);

        return update;
    }

    @Test
    @DisplayName("Ручной ввод комиссии, валидное значение")
    void handle_admin_manualFee_success() {
        Long chatId = 2L;
        User user = mockUser(true, false, true);

        Update update = mockUpdate(chatId, "3.5");

        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(setFeeStateService.getState(chatId)).thenReturn(SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        verify(settingsService).setGlobalConversionFee(3.5);
        verify(setFeeStateService).clearState(chatId);

        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.success");
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: отрицательное значение")
    void handle_admin_manualFee_negative() {
        Long chatId = 3L;
        User user = mockUser(true, false, true);

        Update update = mockUpdate(chatId, "-1");

        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(setFeeStateService.getState(chatId)).thenReturn(SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.invalid_value");
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: больше 100")
    void handle_admin_manualFee_tooBig() {
        Long chatId = 4L;
        User user = mockUser(true, false, true);

        Update update = mockUpdate(chatId, "101");

        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(setFeeStateService.getState(chatId)).thenReturn(SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.invalid_value");
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: не число")
    void handle_admin_manualFee_notNumber() {
        Long chatId = 5L;
        User user = mockUser(true, false, true);
        Update update = mockUpdate(chatId, "abc");
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(setFeeStateService.getState(chatId)).thenReturn(SetFeeState.WAITING_MANUAL_FEE);
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.invalid_value");
    }

    @Test
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        Long chatId = 6L;
        User user = mockUser(false, false, true);

        Update update = mockUpdate(chatId, "/setFee");

        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.no_access");
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        Long chatId = 7L;
        User user = mockUser(true, true, true);

        Update update = mockUpdate(chatId, "/setFee");

        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.no_access");
    }

    @Test
    @DisplayName("Нет доступа: не верифицирован")
    void handle_notVerified() {
        Long chatId = 8L;
        User user = mockUser(true, false, false);

        Update update = mockUpdate(chatId, "/setFee");

        when(userService.findUserByChatId(chatId)).thenReturn(user);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("command.setFee.no_access");
    }

    @Test
    @DisplayName("matches: /setFee")
    void matches_command() {
        Update update = mockUpdate(1L, "/setFee");

        when(setFeeStateService.getState(1L)).thenReturn(SetFeeState.NONE);
        assertThat(command.matches(update)).isTrue();
    }

    @Test
    @DisplayName("matches: WAITING_MANUAL_FEE")
    void matches_waitingManualFee() {
        Update update = mockUpdate(2L, "3.5");

        when(setFeeStateService.getState(2L)).thenReturn(SetFeeState.WAITING_MANUAL_FEE);
        assertThat(command.matches(update)).isTrue();
    }

    @Test
    @DisplayName("matches: NONE")
    void matches_none() {
        Update update = mockUpdate(3L, "other");
        
        when(setFeeStateService.getState(3L)).thenReturn(SetFeeState.NONE);
        assertThat(command.matches(update)).isFalse();
    }

    @Test
    @DisplayName("getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/setFee");
        assertThat(command.getDescription()).isEqualTo("command.setFee.description");
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