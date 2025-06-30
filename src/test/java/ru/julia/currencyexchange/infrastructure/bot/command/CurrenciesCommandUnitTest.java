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
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyCallbackHandler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CurrenciesCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private CurrencyExchangeService currencyExchangeService;
    @Mock
    private UserService userService;
    @Mock
    private CurrencyMessageBuilder currencyMessageBuilder;
    @Mock
    private PaginationKeyboardBuilder paginationKeyboardBuilder;
    @Mock
    private CurrencyCallbackHandler callbackHandler;

    private CurrenciesCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CurrenciesCommand(
                messageConverter,
                currencyExchangeService,
                userService,
                currencyMessageBuilder,
                paginationKeyboardBuilder,
                callbackHandler
        );
    }

    @Test
    @DisplayName("Валидный пользователь, есть валюты (обычный режим)")
    void validUser_withCurrencies() {
        Update update = mockUpdate(1L, "user");
        when(userService.existsByChatId(1L)).thenReturn(true);
        User user = validUser();
        when(userService.findUserByChatId(1L)).thenReturn(user);
        List<Currency> currencies = List.of(
                new Currency("USD", "Доллар", BigDecimal.valueOf(100)),
                new Currency("EUR", "Евро", BigDecimal.valueOf(90))
        );
        when(currencyExchangeService.getAllCurrencies()).thenReturn(currencies);
        when(currencyMessageBuilder.buildCurrenciesMessage(any(), anyInt(), anyBoolean(), anyInt())).thenReturn("currencies");
        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(paginationKeyboardBuilder.buildPaginationKeyboard(anyInt(), anyInt(), anyInt())).thenReturn(keyboard);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("currencies");
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
        return update;
    }

    private User validUser() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);
        user.setBanned(false);
        return user;
    }

    @Test
    @DisplayName("Валидный пользователь, валют нет")
    void validUser_noCurrencies() {
        Update update = mockUpdate(2L, "user");
        when(userService.existsByChatId(2L)).thenReturn(true);
        User user = validUser();
        when(userService.findUserByChatId(2L)).thenReturn(user);
        when(currencyExchangeService.getAllCurrencies()).thenReturn(Collections.emptyList());
        when(messageConverter.resolve("command.currencies.no_currencies")).thenReturn("no_currencies");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("no_currencies");
    }

    @Test
    @DisplayName("Пользователь не найден")
    void userNotFound() {
        Update update = mockUpdate(3L, "user");
        when(userService.existsByChatId(3L)).thenReturn(false);
        when(messageConverter.resolve("command.currencies.error")).thenReturn("error");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }

    @Test
    @DisplayName("Пользователь забанен/удалён/не верифицирован")
    void userBannedOrDeletedOrNotVerified() {
        Update update = mockUpdate(4L, "user");
        when(userService.existsByChatId(4L)).thenReturn(true);
        User user = new User();
        user.setBanned(true);
        user.setDeleted(false);
        user.setVerified(true);
        when(userService.findUserByChatId(4L)).thenReturn(user);
        when(messageConverter.resolve("command.currencies.error")).thenReturn("error");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");

        user.setBanned(false);
        user.setDeleted(true);
        when(userService.findUserByChatId(4L)).thenReturn(user);
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");

        user.setDeleted(false);
        user.setVerified(false);
        when(userService.findUserByChatId(4L)).thenReturn(user);
        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }

    @Test
    @DisplayName("Ошибка в сервисах")
    void serviceException() {
        Update update = mockUpdate(5L, "user");
        when(userService.existsByChatId(5L)).thenThrow(new RuntimeException());
        when(messageConverter.resolve("command.currencies.error")).thenReturn("error");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("error");
    }

    @Test
    void getCommandAndDescription() {
        assertThat(command.getCommand()).isEqualTo("/currencies");
        assertThat(command.getDescription()).isEqualTo("command.currencies.description");
        assertThat(command.getCallbackHandler()).isEqualTo(callbackHandler);
    }
} 