package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.CurrencyConvertService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.CurrencyConvertKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class ConvertCommandIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private CurrencyConvertService currencyConvertService;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private CurrencyConvertKeyboardBuilder keyboardBuilder;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private ConvertCommand command;

    @BeforeEach
    void setUp() {
        databaseCleaner.resetDatabase();
        command = new ConvertCommand(messageConverter, currencyConvertService, userService, keyboardBuilder);
    }

    @Nested
    @DisplayName("handle: положительные и отрицательные сценарии")
    class Handle {
        @Test
        @DisplayName("/convert: нет валют")
        void noCurrencies() {
            User user = createUser(1L, "user", "user@mail.com", true, false, false);
            Update update = mockUpdate(1L, "user", "/convert");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Нет доступных валют");
        }

        @Test
        @DisplayName("/convert: есть валюты, выбор валюты")
        void showFromCurrencySelection() {
            User user = createUser(2L, "user", "user2@mail.com", true, false, false);
            currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
            currencyRepository.save(new Currency("EUR", "Евро", BigDecimal.valueOf(90)));
            Update update = mockUpdate(2L, "user", "/convert");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Конвертация валют");
            assertThat(msg.getParameters().get("reply_markup")).isInstanceOf(InlineKeyboardMarkup.class);
        }

        @Test
        @DisplayName("/convert USD: несуществующая валюта")
        void handleSingleCurrency_notFound() {
            User user = createUser(3L, "user", "user3@mail.com", true, false, false);
            Update update = mockUpdate(3L, "user", "/convert USD");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("не найдена");
        }

        @Test
        @DisplayName("/convert USD EUR: несуществующая валюта")
        void handleCurrencySelection_notFound() {
            User user = createUser(4L, "user", "user4@mail.com", true, false, false);
            currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
            Update update = mockUpdate(4L, "user", "/convert USD EUR");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("не найдена");
        }

        @Test
        @DisplayName("/convert USD EUR 100: успешная конвертация")
        void handleFullConvert_success() {
            User user = createUser(5L, "user", "user5@mail.com", true, false, false);
            currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
            currencyRepository.save(new Currency("EUR", "Евро", BigDecimal.valueOf(90)));
            Update update = mockUpdate(5L, "user", "/convert USD EUR 100");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Конвертация валют");
            assertThat(msg.getParameters().get("reply_markup")).isInstanceOf(InlineKeyboardMarkup.class);
            // Историю можно проверить через отдельный тест или репозиторий
        }

        @Test
        @DisplayName("/convert USD EUR 100: невалидная сумма")
        void handleFullConvert_invalidAmount() {
            User user = createUser(6L, "user", "user6@mail.com", true, false, false);
            currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
            currencyRepository.save(new Currency("EUR", "Евро", BigDecimal.valueOf(90)));
            Update update = mockUpdate(6L, "user", "/convert USD EUR -1");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Неверная сумма");
        }

        @Test
        @DisplayName("/convert: пользователь не найден")
        void userNotFound() {
            Update update = mockUpdate(7L, "user", "/convert");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Ошибка");
        }

        @Test
        @DisplayName("/convert: пользователь забанен")
        void userBanned() {
            User user = createUser(8L, "user", "user8@mail.com", true, false, true);
            currencyRepository.save(new Currency("USD", "Доллар", BigDecimal.valueOf(100)));
            Update update = mockUpdate(8L, "user", "/convert");
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).asString().contains("Ошибка");
        }
    }

    @Test
    @DisplayName("isAccessible: все ветки")
    void isAccessibleVariants() {
        User user = createUser(9L, "user", "user9@mail.com", true, false, false);
        assertThat(command.isAccessible(user)).isTrue();
        user.setDeleted(true);
        assertThat(command.isAccessible(user)).isFalse();
        user.setDeleted(false);
        user.setVerified(false);
        assertThat(command.isAccessible(user)).isFalse();
        assertThat(command.isAccessible(null)).isFalse();
    }

    private User createUser(Long chatId, String username, String email, boolean verified, boolean deleted, boolean banned) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setVerified(verified);
        user.setDeleted(deleted);
        user.setBanned(banned);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("matches: все ветки")
    void matchesVariants() {
        Update update = mockUpdate(10L, "user", "/convert");
        assertThat(command.matches(update)).isTrue();
        Update update2 = mockUpdate(11L, "user", "any");
        currencyConvertService.setState(11L, ConversionState.WAITING_AMOUNT);
        assertThat(command.matches(update2)).isTrue();
        Update update3 = org.mockito.Mockito.mock(Update.class);
        org.mockito.Mockito.when(update3.message()).thenReturn(null);
        assertThat(command.matches(update3)).isFalse();
    }

    private Update mockUpdate(Long chatId, String username, String text) {
        Update update = org.mockito.Mockito.mock(Update.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        Chat chat = org.mockito.Mockito.mock(Chat.class);
        org.mockito.Mockito.when(update.message()).thenReturn(message);
        org.mockito.Mockito.when(message.chat()).thenReturn(chat);
        org.mockito.Mockito.when(chat.id()).thenReturn(chatId);
        org.mockito.Mockito.when(chat.username()).thenReturn(username);
        org.mockito.Mockito.when(message.text()).thenReturn(text);
        return update;
    }
} 