package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.FindByDateCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindByDateCommandUnitTest {
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
    @InjectMocks
    private FindByDateCommand command;

    @Test
    @DisplayName("Есть конвертации")
    void handle_success() {
        Long chatId = 1L;
        String username = "user";
        String date = "2024-06-30";
        String text = "/findByDate " + date;

        Update update = mockUpdate(chatId, username, text);

        User user = mock(User.class);
        when(user.getId()).thenReturn("u1");
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);

        List<CurrencyConversion> conversions = List.of(mock(CurrencyConversion.class));
        when(currencyExchangeService.findByCurrencyDate("u1", date)).thenReturn(conversions);
        when(historyMessageBuilder.buildFindByDateMessage(anyList(), eq(0), anyBoolean(), anyInt(), eq(date))).thenReturn("MSG");

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(paginationKeyboardBuilder.buildFindByDatePaginationKeyboard(anyInt(), anyInt(), anyInt(), eq(date))).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("MSG");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
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
    @DisplayName("Не зарегистрирован")
    void handle_notRegistered() {
        Long chatId = 2L;

        Update update = mockUpdate(chatId, "user", "/findByDate 2024-06-30");

        when(userService.existsByChatId(chatId)).thenReturn(false);
        when(messageConverter.resolve("command.findByDate.error")).thenReturn("ERROR");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Забанен/удалён/не верифицирован")
    void handle_bannedOrDeletedOrUnverified() {
        Long chatId = 3L;

        Update update = mockUpdate(chatId, "user", "/findByDate 2024-06-30");

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(true);
        when(messageConverter.resolve("command.findByDate.error")).thenReturn("ERROR");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Неверный формат команды")
    void handle_invalidFormat() {
        Long chatId = 4L;

        Update update = mockUpdate(chatId, "user", "/findByDate");

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(messageConverter.resolve("command.findByDate.usage")).thenReturn("USAGE");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("USAGE");
    }

    @Test
    @DisplayName("Неверная дата")
    void handle_invalidDate() {
        Long chatId = 5L;

        Update update = mockUpdate(chatId, "user", "/findByDate not-a-date");

        User user = mock(User.class);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(messageConverter.resolve("command.findByDate.invalid_date")).thenReturn("BAD_DATE");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("BAD_DATE");
    }

    @Test
    @DisplayName("Нет конвертаций")
    void handle_noConversions() {
        Long chatId = 6L;
        String date = "2024-06-30";

        Update update = mockUpdate(chatId, "user", "/findByDate " + date);

        User user = mock(User.class);
        when(user.getId()).thenReturn("u6");
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(currencyExchangeService.findByCurrencyDate("u6", date)).thenReturn(emptyList());
        when(messageConverter.resolve(eq("command.findByDate.no_conversions"), anyMap())).thenReturn("NO_CONV");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("NO_CONV");
    }

    @Test
    @DisplayName("Ошибка сервиса")
    void handle_serviceError() {
        Long chatId = 7L;
        String date = "2024-06-30";

        Update update = mockUpdate(chatId, "user", "/findByDate " + date);

        User user = mock(User.class);
        when(user.getId()).thenReturn("u7");
        when(user.isBanned()).thenReturn(false);
        when(user.isDeleted()).thenReturn(false);
        when(user.isVerified()).thenReturn(true);
        when(userService.existsByChatId(chatId)).thenReturn(true);
        when(userService.findUserByChatId(chatId)).thenReturn(user);
        when(currencyExchangeService.findByCurrencyDate("u7", date)).thenThrow(new RuntimeException("fail"));
        when(messageConverter.resolve("command.findByDate.error")).thenReturn("ERROR");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Проверка getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/findByDate");
        assertThat(command.getDescription()).isEqualTo("command.findByDate.description");
    }

    @Test
    @DisplayName("Проверка isAccessible — true")
    void isAccessible_true() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);

        assertThat(command.isAccessible(user)).isTrue();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (null)")
    void isAccessible_null() {
        assertThat(command.isAccessible(null)).isFalse();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (удалён)")
    void isAccessible_deleted() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(true);

        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (не верифицирован)")
    void isAccessible_notVerified() {
        User user = new User();
        user.setVerified(false);
        user.setDeleted(false);

        assertThat(command.isAccessible(user)).isFalse();
    }
} 