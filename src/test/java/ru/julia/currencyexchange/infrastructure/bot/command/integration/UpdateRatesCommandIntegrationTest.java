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
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.UpdateRatesCommand;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRoleRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UpdateRatesCommandIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private CurrencyExchangeService currencyExchangeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private UpdateRatesCommand command;

    @BeforeEach
    void setUp() {
        command = new UpdateRatesCommand(messageConverter, currencyExchangeService, userService);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Обычный вызов")
    void handle_admin_success() {
        User user = createUser(1L, true, false, false);

        Update update = mockUpdate(1L, "admin");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("Курсы валют успешно обновлены");
    }

    private User createUser(Long chatId, boolean admin, boolean deleted, boolean banned) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername("user" + chatId);
        user.setEmail("user" + chatId + "@mail.com");
        user.setPassword("testpassword");
        user.setDeleted(deleted);
        user.setBanned(banned);
        user.setVerified(true);
        user = userRepository.save(user);

        if (admin) {
            Role role = roleRepository.findByRoleName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            UserRole userRole = new UserRole(user, role);
            userRoleRepository.save(userRole);
        }

        return user;
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
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        User user = createUser(2L, false, false, false);

        Update update = mockUpdate(2L, "user2");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.updateRates.error"));
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        User user = createUser(3L, true, true, false);

        Update update = mockUpdate(3L, "user3");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.updateRates.error"));
    }

    @Test
    @DisplayName("Нет доступа: забанен")
    void handle_banned() {
        User user = createUser(4L, true, false, true);

        Update update = mockUpdate(4L, "user4");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.updateRates.error"));
    }

    @Test
    @DisplayName("Пользователь не зарегистрирован")
    void handle_notRegistered() {
        Update update = mockUpdate(5L, "user5");

        SendMessage msg = command.handle(update);

        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.updateRates.error"));
    }
} 