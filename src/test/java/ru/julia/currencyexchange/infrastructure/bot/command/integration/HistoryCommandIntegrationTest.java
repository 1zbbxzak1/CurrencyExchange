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
import org.springframework.test.annotation.DirtiesContext;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.HistoryCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.HistoryCallbackHandler;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ConversionRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HistoryCommandIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private CurrencyExchangeService currencyExchangeService;
    @Autowired
    private UserService userService;
    @Autowired
    private HistoryMessageBuilder historyMessageBuilder;
    @Autowired
    private PaginationKeyboardBuilder paginationKeyboardBuilder;
    @Autowired
    private HistoryCallbackHandler historyCallbackHandler;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ConversionRepository conversionRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private HistoryCommand command;

    @BeforeEach
    void setUp() {
        command = new HistoryCommand(
                messageConverter, currencyExchangeService,
                userService, historyMessageBuilder,
                paginationKeyboardBuilder, historyCallbackHandler
        );
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Есть конвертации")
    void handle_success() {
        User user = createUser(1L, false, false, true);
        Currency usd = createCurrency("USD", "Доллар", BigDecimal.valueOf(100));
        Currency eur = createCurrency("EUR", "Евро", BigDecimal.valueOf(90));

        for (int i = 0; i < 6; i++) {
            createConversion(user, usd, eur, BigDecimal.valueOf(10 + i), BigDecimal.valueOf(900 + i), LocalDateTime.now());
        }

        Update update = mockUpdate(1L, user.getUsername());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.history.title"));
        assertThat(msg.getParameters().get("reply_markup")).isNotNull();
    }

    private User createUser(Long chatId, boolean banned, boolean deleted, boolean verified) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername("user" + chatId);
        user.setBanned(banned);
        user.setDeleted(deleted);
        user.setVerified(verified);
        user.setEmail("user" + chatId + "@mail.com");
        user.setPassword("testpassword");

        return userRepository.save(user);
    }

    private Currency createCurrency(String code, String name, BigDecimal rate) {
        Currency currency = new Currency(code, name, rate);
        return currencyRepository.save(currency);
    }

    private CurrencyConversion createConversion(User user, Currency from,
                                                Currency to, BigDecimal amount,
                                                BigDecimal result, LocalDateTime dateTime) {
        CurrencyConversion conversion = new CurrencyConversion(user, from, to, amount, result, BigDecimal.ONE);
        try {
            var field = CurrencyConversion.class.getDeclaredField("timestamp");
            field.setAccessible(true);
            field.set(conversion, dateTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conversionRepository.save(conversion);
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

    @Test
    @DisplayName("Не зарегистрирован")
    void handle_notRegistered() {
        Update update = mockUpdate(2L, "user2");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.error"));
    }

    @Test
    @DisplayName("Забанен")
    void handle_banned() {
        User user = createUser(3L, true, false, true);

        Update update = mockUpdate(3L, user.getUsername());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.error"));
    }

    @Test
    @DisplayName("Удалён")
    void handle_deleted() {
        User user = createUser(4L, false, true, true);

        Update update = mockUpdate(4L, user.getUsername());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.error"));
    }

    @Test
    @DisplayName("Не верифицирован")
    void handle_notVerified() {
        User user = createUser(5L, false, false, false);

        Update update = mockUpdate(5L, user.getUsername());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.error"));
    }

    @Test
    @DisplayName("Нет конвертаций")
    void handle_noConversions() {
        User user = createUser(6L, false, false, true);

        Update update = mockUpdate(6L, user.getUsername());

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.no_conversions"));
    }

    @Test
    @DisplayName("Ошибка — пользователь удалён после создания")
    void handle_error() {
        User user = createUser(7L, false, false, true);
        userRepository.delete(user);

        Update update = mockUpdate(7L, "user7");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.history.error"));
    }
} 