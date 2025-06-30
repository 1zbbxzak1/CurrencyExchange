package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyMessageBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.PaginationKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.CurrencyCallbackHandler;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class CurrenciesCommandIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private CurrencyExchangeService currencyExchangeService;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private CurrencyMessageBuilder currencyMessageBuilder;
    @Autowired
    private PaginationKeyboardBuilder paginationKeyboardBuilder;
    @Autowired
    private CurrencyCallbackHandler callbackHandler;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private CurrenciesCommand command;

    @BeforeEach
    void setUp() {
        databaseCleaner.resetDatabase();
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
    @DisplayName("Валидный пользователь, валют нет")
    void validUser_noCurrencies() {
        User user = createUser(2L, "user", "user2@mail.com", false);
        Update update = mockUpdate(2L, "user");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("Валюты не найдены");
    }

    private User createUser(Long chatId, String username, String email, boolean banned) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setVerified(true);
        user.setDeleted(false);
        user.setBanned(banned);

        return userRepository.save(user);
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

    @Test
    @DisplayName("Пользователь не найден")
    void userNotFound() {
        Update update = mockUpdate(3L, "user");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencies.error"));
    }

    @Test
    @DisplayName("Пользователь забанен/удалён/не верифицирован")
    void userBannedOrDeletedOrNotVerified() {
        User user = createUser(4L, "user", "user4@mail.com", true);
        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));

        Update update = mockUpdate(4L, "user");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencies.error"));

        user.setBanned(false);
        user.setDeleted(true);
        userRepository.save(user);

        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencies.error"));

        user.setDeleted(false);
        user.setVerified(false);
        userRepository.save(user);

        msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.currencies.error"));
    }

    @Test
    @DisplayName("Валидный пользователь, есть валюты (мало валют, без пагинации)")
    void validUser_withFewCurrencies() {
        User user = createUser(1L, "user", "user@mail.com", false);
        currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
        currencyRepository.save(new Currency("EUR", "Евро", BigDecimal.valueOf(90)));

        Update update = mockUpdate(1L, "user");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("КУРСЫ ВАЛЮТ");
        assertThat(msg.getParameters().get("reply_markup")).isNull();
    }

    @Test
    @DisplayName("Валидный пользователь, много валют (есть пагинация)")
    void validUser_withManyCurrencies() {
        User user = createUser(10L, "user10", "user10@mail.com", false);

        for (int i = 0; i < 15; i++) {
            currencyRepository.save(new Currency("CUR" + i, "TestCurrency" + i, BigDecimal.valueOf(10 + i)));
        }

        Update update = mockUpdate(10L, "user10");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("КУРСЫ ВАЛЮТ");
        assertThat(msg.getParameters().get("reply_markup")).isInstanceOf(InlineKeyboardMarkup.class);
    }
} 