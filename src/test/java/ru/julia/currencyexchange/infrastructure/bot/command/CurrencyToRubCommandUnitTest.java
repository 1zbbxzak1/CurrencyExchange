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
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyToRubKeyboardBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CurrencyToRubCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private CurrencyToRubService currencyToRubService;
    @Mock
    private UserService userService;
    @Mock
    private CurrencyToRubKeyboardBuilder keyboardBuilder;

    private CurrencyToRubCommand command;

    @BeforeEach
    void setUp() {
        openMocks(this);
        command = new CurrencyToRubCommand(
                messageConverter,
                currencyToRubService,
                userService,
                keyboardBuilder
        );
    }

    @Test
    @DisplayName("Пользователь не найден")
    void userNotFound() {
        Update update = mockUpdate(1L, "/currencyToRub USD");

        when(userService.existsByChatId(1L)).thenReturn(false);
        when(messageConverter.resolve("command.currencyToRub.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
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
    @DisplayName("Пользователь забанен/удален/не верифицирован")
    void userBannedOrDeletedOrUnverified() {
        Update update = mockUpdate(2L, "/currencyToRub USD");

        when(userService.existsByChatId(2L)).thenReturn(true);

        User user = new User();
        user.setBanned(true);
        user.setDeleted(false);
        user.setVerified(true);

        when(userService.findUserByChatId(2L)).thenReturn(user);
        when(messageConverter.resolve("command.currencyToRub.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");

        user.setBanned(false);
        user.setDeleted(true);

        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");

        user.setDeleted(false);
        user.setVerified(false);

        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }

    @Test
    @DisplayName("Нет кода валюты — показать клавиатуру выбора, валют нет")
    void noCurrencyCode_noCurrencies() {
        Update update = mockUpdate(3L, "/currencyToRub");

        when(userService.existsByChatId(3L)).thenReturn(true);

        User user = validUser();
        when(userService.findUserByChatId(3L)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(anyLong(), anyString());

        when(currencyToRubService.hasCurrencies()).thenReturn(false);
        when(messageConverter.resolve("command.currencyToRub.no_currencies")).thenReturn("no_currencies");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_currencies");
    }

    private User validUser() {
        User user = new User();
        user.setBanned(false);
        user.setDeleted(false);
        user.setVerified(true);

        return user;
    }

    @Test
    @DisplayName("Нет кода валюты — показать клавиатуру выбора, валюты есть")
    void noCurrencyCode_withCurrencies() {
        Update update = mockUpdate(4L, "/currencyToRub");

        when(userService.existsByChatId(4L)).thenReturn(true);

        User user = validUser();
        when(userService.findUserByChatId(4L)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(anyLong(), anyString());

        when(currencyToRubService.hasCurrencies()).thenReturn(true);

        List<Currency> popular = List.of(new Currency("USD", "Доллар", BigDecimal.ONE));
        when(currencyToRubService.getPopularCurrencies()).thenReturn(popular);

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardBuilder.buildPopularCurrenciesKeyboard(popular)).thenReturn(keyboard);

        when(messageConverter.resolve("command.currencyToRub.selection.title")).thenReturn("title");
        when(messageConverter.resolve("command.currencyToRub.selection.popular_subtitle")).thenReturn("popular");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("title");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
    }

    @Test
    @DisplayName("Валюта не найдена")
    void currencyNotFound() {
        Update update = mockUpdate(5L, "/currencyToRub XXX");

        when(userService.existsByChatId(5L)).thenReturn(true);

        User user = validUser();
        when(userService.findUserByChatId(5L)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(anyLong(), anyString());

        when(currencyToRubService.getCurrencyByCode("XXX")).thenReturn(null);
        when(messageConverter.resolve(eq("command.currencyToRub.not_found"), anyMap())).thenReturn("not_found");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("not_found");
    }

    @Test
    @DisplayName("Успешный сценарий — валюта найдена")
    void success() {
        Update update = mockUpdate(6L, "/currencyToRub USD");

        when(userService.existsByChatId(6L)).thenReturn(true);

        User user = validUser();
        when(userService.findUserByChatId(6L)).thenReturn(user);

        doNothing().when(userService).updateUsernameIfChanged(anyLong(), anyString());

        Currency currency = new Currency("USD", "Доллар", BigDecimal.valueOf(100));
        when(currencyToRubService.getCurrencyByCode("USD")).thenReturn(currency);
        when(currencyToRubService.buildCurrencyToRubMessage(currency)).thenReturn("result");

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardBuilder.buildBackKeyboard()).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("result");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
    }

    @Test
    @DisplayName("handle: Exception — возвращает ошибку")
    void handle_exception() {
        Update update = mockUpdate(7L, "/currencyToRub USD");

        when(userService.existsByChatId(7L)).thenThrow(new RuntimeException("fail"));
        when(messageConverter.resolve("command.currencyToRub.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }
} 