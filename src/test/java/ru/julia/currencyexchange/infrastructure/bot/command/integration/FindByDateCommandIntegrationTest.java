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
import ru.julia.currencyexchange.infrastructure.bot.command.FindByDateCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.HistoryMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ConversionRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FindByDateCommandIntegrationTest extends IntegrationTestBase {
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
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ConversionRepository conversionRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private FindByDateCommand command;

    @BeforeEach
    void setUp() {
        command = new FindByDateCommand(messageConverter, currencyExchangeService,
                userService, historyMessageBuilder, paginationKeyboardBuilder);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Есть конвертации")
    void handle_success() {
        User user = createUser(1L, false, false, true);

        Currency usd = createCurrency("USD", "Доллар", BigDecimal.valueOf(100));
        Currency eur = createCurrency("EUR", "Евро", BigDecimal.valueOf(90));
        LocalDate date = LocalDate.of(2024, 6, 30);

        for (int i = 0; i < 6; i++) {
            createConversion(user, usd, eur, BigDecimal.valueOf(10 + i), BigDecimal.valueOf(900 + i), date.atStartOfDay());
        }

        Update update = mockUpdate(1L, user.getUsername(), "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.findByDate.title"));
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
        Update update = mockUpdate(2L, "user", "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.error"));
    }

    @Test
    @DisplayName("Забанен")
    void handle_banned() {
        createUser(3L, true, false, true);

        Update update = mockUpdate(3L, "user3", "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.error"));
    }

    @Test
    @DisplayName("Удалён")
    void handle_deleted() {
        createUser(4L, false, true, true);

        Update update = mockUpdate(4L, "user4", "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.error"));
    }

    @Test
    @DisplayName("Не верифицирован")
    void handle_notVerified() {
        createUser(5L, false, false, false);

        Update update = mockUpdate(5L, "user5", "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.error"));
    }

    @Test
    @DisplayName("Неверный формат команды")
    void handle_invalidFormat() {
        User user = createUser(6L, false, false, true);

        Update update = mockUpdate(6L, user.getUsername(), "/findByDate");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.usage"));
    }

    @Test
    @DisplayName("Неверная дата")
    void handle_invalidDate() {
        User user = createUser(7L, false, false, true);

        Update update = mockUpdate(7L, user.getUsername(), "/findByDate not-a-date");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.invalid_date"));
    }

    @Test
    @DisplayName("Нет конвертаций")
    void handle_noConversions() {
        User user = createUser(8L, false, false, true);
        Update update = mockUpdate(8L, user.getUsername(), "/findByDate 2024-06-30");
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.findByDate.no_conversions", Map.of("date", "30.06.2024")));
    }

    @Test
    @DisplayName("Ошибка — пользователь удалён после создания")
    void handle_serviceError() {
        User user = createUser(9L, false, false, true);
        userRepository.delete(user);

        Update update = mockUpdate(9L, "user9", "/findByDate 2024-06-30");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.findByDate.error"));
    }
} 