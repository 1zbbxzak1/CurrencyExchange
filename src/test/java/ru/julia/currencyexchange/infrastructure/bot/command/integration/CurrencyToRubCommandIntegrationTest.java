package ru.julia.currencyexchange.infrastructure.bot.command.integration;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyToRubService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.CurrencyToRubCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyToRubKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@Transactional
class CurrencyToRubCommandIntegrationTest extends IntegrationTestBase {
    @Autowired
    private UserService userService;
    @Autowired
    private CurrencyToRubService currencyToRubService;
    @Autowired
    private CurrencyToRubKeyboardBuilder keyboardBuilder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private MessageConverter messageConverter;

    private CurrencyToRubCommand command;

    @BeforeEach
    void setUp() {
        command = new CurrencyToRubCommand(
                messageConverter,
                currencyToRubService,
                userService,
                keyboardBuilder
        );
        userRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    @DisplayName("Пользователь не найден")
    void userNotFound() {
        var update = mockUpdate(1L, "/currencyToRub USD");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencyToRub.error"));
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
    @DisplayName("Нет кода валюты — показать клавиатуру выбора, валют нет")
    void noCurrencyCode_noCurrencies() {
        createUser(3L);

        var update = mockUpdate(3L, "/currencyToRub");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencyToRub.no_currencies"));
    }

    private User createUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername("user" + chatId);
        user.setBanned(false);
        user.setDeleted(false);
        user.setVerified(true);
        user.setEmail("user" + chatId + "@mail.com");
        user.setPassword("testpassword");

        return userRepository.save(user);
    }

    @Test
    @DisplayName("Нет кода валюты — показать клавиатуру выбора, валюты есть")
    void noCurrencyCode_withCurrencies() {
        createUser(4L);
        createCurrency(BigDecimal.valueOf(100));

        var update = mockUpdate(4L, "/currencyToRub");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.currencyToRub.selection.title"));
        assertThat(msg.getParameters().get("reply_markup")).isNotNull();
    }

    private Currency createCurrency(BigDecimal rate) {
        Currency currency = new Currency("USD", "Доллар", rate);
        setLastUpdated(currency, LocalDateTime.now());

        return currencyRepository.save(currency);
    }

    private void setLastUpdated(Currency currency, LocalDateTime dateTime) {
        try {
            var field = Currency.class.getDeclaredField("lastUpdated");
            field.setAccessible(true);
            field.set(currency, dateTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Валюта не найдена")
    void currencyNotFound() {
        createUser(5L);

        var update = mockUpdate(5L, "/currencyToRub XXX");
        String expected = messageConverter.resolve("command.currencyToRub.not_found", Map.of("currency_code", "XXX"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(expected);
    }

    @Test
    @DisplayName("Успешный сценарий — валюта найдена")
    void success() {
        createUser(6L);

        Currency currency = createCurrency(BigDecimal.valueOf(100));
        var update = mockUpdate(6L, "/currencyToRub USD");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(currency.getCode());
        assertThat(msg.getParameters().get("reply_markup")).isNotNull();
    }

    @Test
    @DisplayName("handle: Exception — возвращает ошибку")
    void handle_exception() {
        createUser(7L);

        var update = mockUpdate(7L, "/currencyToRub USD");

        userRepository.deleteAll();

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencyToRub.error"));
    }
} 