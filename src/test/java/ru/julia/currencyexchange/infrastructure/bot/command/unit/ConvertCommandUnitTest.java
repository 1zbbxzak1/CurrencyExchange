package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.ConvertCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyConvertKeyboardBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ConvertCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private CurrencyConvertService currencyConvertService;
    @Mock
    private UserService userService;
    @Mock
    private CurrencyConvertKeyboardBuilder keyboardBuilder;
    private ConvertCommand command;

    @BeforeEach
    void setUp() {
        openMocks(this);
        command = new ConvertCommand(messageConverter, currencyConvertService, userService, keyboardBuilder);
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

    private User validUser() {
        User user = new User();
        user.setVerified(true);
        user.setDeleted(false);

        return user;
    }

    @Nested
    @DisplayName("handle: основные сценарии")
    class Handle {
        @Test
        @DisplayName("Пользователь не найден")
        void userNotFound() {
            Update update = mockUpdate(1L, "/convert");

            when(userService.existsByChatId(1L)).thenReturn(false);
            when(messageConverter.resolve("command.convert.error")).thenReturn("error");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("error");
        }

        @Test
        @DisplayName("Пользователь забанен/удалён/не верифицирован")
        void userBannedOrDeletedOrNotVerified() {
            Update update = mockUpdate(2L, "/convert");

            when(userService.existsByChatId(2L)).thenReturn(true);

            User user = new User();
            user.setBanned(true);
            user.setDeleted(false);
            user.setVerified(true);

            when(userService.findUserByChatId(2L)).thenReturn(user);
            when(messageConverter.resolve("command.convert.error")).thenReturn("error");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("error");
        }

        @Test
        @DisplayName("WAITING_AMOUNT: невалидная сумма")
        void waitingAmount_invalidAmount() {
            Update update = mockUpdate(3L, "123");

            when(userService.existsByChatId(3L)).thenReturn(true);

            User user = validUser();

            when(userService.findUserByChatId(3L)).thenReturn(user);
            when(currencyConvertService.getState(3L)).thenReturn(ConversionState.WAITING_AMOUNT);
            when(currencyConvertService.isValidAmount("123")).thenReturn(false);
            when(messageConverter.resolve("command.convert.invalid_amount")).thenReturn("invalid_amount");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("invalid_amount");
        }

        @Test
        @DisplayName("WAITING_AMOUNT: успешная конвертация")
        void waitingAmount_success() {
            Update update = mockUpdate(4L, "100");

            when(userService.existsByChatId(4L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(4L)).thenReturn(user);

            when(currencyConvertService.getState(4L)).thenReturn(ConversionState.WAITING_AMOUNT);
            when(currencyConvertService.isValidAmount("100")).thenReturn(true);

            CurrencyConvertService.ConversionData data = new CurrencyConvertService.ConversionData("USD", "EUR");
            when(currencyConvertService.getData(4L)).thenReturn(data);
            when(currencyConvertService.parseAmount("100")).thenReturn(BigDecimal.valueOf(100));

            CurrencyConversion conversion = mock(CurrencyConversion.class);
            when(currencyConvertService.convertCurrency(4L, "USD", "EUR", BigDecimal.valueOf(100))).thenReturn(conversion);
            when(currencyConvertService.buildConversionMessage(conversion)).thenReturn("result");

            InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
            when(keyboardBuilder.buildBackKeyboard()).thenReturn(keyboard);

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("result");
            assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
        }

        @Test
        @DisplayName("WAITING_AMOUNT: ошибка конвертации")
        void waitingAmount_conversionError() {
            Update update = mockUpdate(5L, "bad");

            when(userService.existsByChatId(5L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(5L)).thenReturn(user);
            when(currencyConvertService.getState(5L)).thenReturn(ConversionState.WAITING_AMOUNT);
            when(currencyConvertService.isValidAmount("bad")).thenReturn(true);

            CurrencyConvertService.ConversionData data = new CurrencyConvertService.ConversionData("USD", "EUR");
            when(currencyConvertService.getData(5L)).thenReturn(data);
            when(currencyConvertService.parseAmount("bad")).thenThrow(new NumberFormatException());

            doNothing().when(currencyConvertService).clearData(5L);

            when(messageConverter.resolve("command.convert.errors.conversion_failed")).thenReturn("fail");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("fail");
        }

        @Test
        @DisplayName("/convert: показать выбор валюты, нет валют")
        void showFromCurrencySelection_noCurrencies() {
            Update update = mockUpdate(6L, "/convert");

            when(userService.existsByChatId(6L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(6L)).thenReturn(user);
            when(currencyConvertService.getState(6L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.hasCurrencies()).thenReturn(false);
            when(messageConverter.resolve("command.convert.no_currencies")).thenReturn("no_currencies");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("no_currencies");
        }

        @Test
        @DisplayName("/convert: показать выбор валюты, есть валюты")
        void showFromCurrencySelection_withCurrencies() {
            Update update = mockUpdate(7L, "/convert");

            when(userService.existsByChatId(7L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(7L)).thenReturn(user);
            when(currencyConvertService.getState(7L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.hasCurrencies()).thenReturn(true);

            List<Currency> popular = List.of(mock(Currency.class));
            when(currencyConvertService.getPopularCurrencies()).thenReturn(popular);

            InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
            when(keyboardBuilder.buildFromCurrencyKeyboard(popular)).thenReturn(keyboard);
            when(messageConverter.resolve("command.convert.selection.title")).thenReturn("title");
            when(messageConverter.resolve("command.convert.selection.from_currency")).thenReturn("from");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("title");
            assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
        }

        @Test
        @DisplayName("/convert USD: несуществующая валюта")
        void handleSingleCurrency_notFound() {
            Update update = mockUpdate(8L, "/convert USD");

            when(userService.existsByChatId(8L)).thenReturn(true);
            User user = validUser();
            when(userService.findUserByChatId(8L)).thenReturn(user);
            when(currencyConvertService.getState(8L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.getCurrencyByCode("USD")).thenReturn(null);
            when(messageConverter.resolve(eq("command.convert.from_not_found"), anyMap())).thenReturn("not_found");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("not_found");
        }

        @Test
        @DisplayName("/convert USD EUR: несуществующая валюта")
        void handleCurrencySelection_notFound() {
            Update update = mockUpdate(9L, "/convert USD EUR");

            when(userService.existsByChatId(9L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(9L)).thenReturn(user);
            when(currencyConvertService.getState(9L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.getCurrencyByCode("USD")).thenReturn(null);
            when(messageConverter.resolve(eq("command.convert.from_not_found"), anyMap())).thenReturn("not_found");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("not_found");
        }

        @Test
        @DisplayName("/convert USD EUR 100: успешная конвертация")
        void handleFullConvert_success() {
            Update update = mockUpdate(10L, "/convert USD EUR 100");

            when(userService.existsByChatId(10L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(10L)).thenReturn(user);
            when(currencyConvertService.getState(10L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.getCurrencyByCode("USD")).thenReturn(mock(Currency.class));
            when(currencyConvertService.getCurrencyByCode("EUR")).thenReturn(mock(Currency.class));
            when(currencyConvertService.isValidAmount("100")).thenReturn(true);
            when(currencyConvertService.parseAmount("100")).thenReturn(BigDecimal.valueOf(100));

            CurrencyConversion conversion = mock(CurrencyConversion.class);
            when(currencyConvertService.convertCurrency(10L, "USD", "EUR", BigDecimal.valueOf(100))).thenReturn(conversion);
            when(currencyConvertService.buildConversionMessage(conversion)).thenReturn("result");

            InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
            when(keyboardBuilder.buildBackKeyboard()).thenReturn(keyboard);

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("result");
            assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
        }

        @Test
        @DisplayName("/convert USD EUR 100: невалидная сумма")
        void handleFullConvert_invalidAmount() {
            Update update = mockUpdate(11L, "/convert USD EUR 100");

            when(userService.existsByChatId(11L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(11L)).thenReturn(user);
            when(currencyConvertService.getState(11L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.getCurrencyByCode("USD")).thenReturn(mock(Currency.class));
            when(currencyConvertService.getCurrencyByCode("EUR")).thenReturn(mock(Currency.class));
            when(currencyConvertService.isValidAmount("100")).thenReturn(false);
            when(messageConverter.resolve("command.convert.invalid_amount")).thenReturn("invalid_amount");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("invalid_amount");
        }

        @Test
        @DisplayName("/convert USD EUR 100: ошибка конвертации")
        void handleFullConvert_error() {
            Update update = mockUpdate(12L, "/convert USD EUR 100");

            when(userService.existsByChatId(12L)).thenReturn(true);

            User user = validUser();
            when(userService.findUserByChatId(12L)).thenReturn(user);
            when(currencyConvertService.getState(12L)).thenReturn(ConversionState.NONE);
            when(currencyConvertService.getCurrencyByCode("USD")).thenReturn(mock(Currency.class));
            when(currencyConvertService.getCurrencyByCode("EUR")).thenReturn(mock(Currency.class));
            when(currencyConvertService.isValidAmount("100")).thenReturn(true);
            when(currencyConvertService.parseAmount("100")).thenReturn(BigDecimal.valueOf(100));
            when(currencyConvertService.convertCurrency(12L, "USD", "EUR", BigDecimal.valueOf(100))).thenThrow(new RuntimeException());
            when(messageConverter.resolve("command.convert.conversion_error")).thenReturn("error");

            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("matches/isAccessible/meta")
    class Meta {
        @Test
        void matches_byCommand() {
            Update update = mockUpdate(1L, "/convert");

            when(currencyConvertService.getState(1L)).thenReturn(ConversionState.NONE);
            assertThat(command.matches(update)).isTrue();
        }

        @Test
        void matches_byState() {
            Update update = mockUpdate(2L, "any");

            when(currencyConvertService.getState(2L)).thenReturn(ConversionState.WAITING_AMOUNT);
            assertThat(command.matches(update)).isTrue();
        }

        @Test
        void matches_null() {
            Update update = mock(Update.class);

            when(update.message()).thenReturn(null);
            assertThat(command.matches(update)).isFalse();
        }

        @Test
        void isAccessible_allVariants() {
            User user = validUser();
            user.setVerified(true);
            user.setDeleted(false);
            assertThat(command.isAccessible(user)).isTrue();

            user.setDeleted(true);
            assertThat(command.isAccessible(user)).isFalse();

            user.setDeleted(false);
            user.setVerified(false);
            assertThat(command.isAccessible(user)).isFalse();
            assertThat(command.isAccessible(null)).isFalse();
        }

        @Test
        void getCommandAndDescription() {
            assertThat(command.getCommand()).isEqualTo("/convert");
            assertThat(command.getDescription()).isEqualTo("command.convert.description");
        }
    }
} 