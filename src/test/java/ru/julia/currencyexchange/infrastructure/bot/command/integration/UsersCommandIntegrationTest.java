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
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.UsersCommand;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRoleRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UsersCommandIntegrationTest {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private UsersCommand command;

    @BeforeEach
    void setUp() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Есть пользователи")
    void handle_admin_success() {
        User admin = createUser(1L, true, false, true);

        createUser(2L, false, false, true);
        createUser(3L, false, false, true);

        Update update = mockUpdate(1L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("СПИСОК ПОЛЬЗОВАТЕЛЕЙ");
        assertThat(msg.getParameters().get("text")).asString().contains("user1");
        assertThat(msg.getParameters().get("text")).asString().contains("user2");
        assertThat(msg.getParameters().get("text")).asString().contains("user3");
        assertThat(msg.getParameters().get("reply_markup")).isNotNull();
    }

    private User createUser(Long chatId, boolean admin, boolean deleted, boolean verified) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername("user" + chatId);
        user.setEmail("user" + chatId + "@mail.com");
        user.setPassword("testpassword");
        user.setDeleted(deleted);
        user.setVerified(verified);
        user = userRepository.save(user);

        if (admin) {
            Role role = roleRepository.findByRoleName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            UserRole userRole = new UserRole(user, role);
            userRoleRepository.save(userRole);
        }

        return user;
    }

    private Update mockUpdate(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }

    @Test
    @DisplayName("Нет пользователей (только админ)")
    void handle_noUsers() {
        User admin = createUser(4L, true, false, true);

        Update update = mockUpdate(4L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("user4");
        assertThat(msg.getParameters().get("reply_markup")).isNotNull();
    }

    @Test
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        User user = createUser(5L, false, false, true);

        Update update = mockUpdate(5L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.users.no_access"));
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        User user = createUser(6L, true, true, true);

        Update update = mockUpdate(6L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.users.no_access"));
    }

    @Test
    @DisplayName("Нет доступа: не верифицирован")
    void handle_notVerified() {
        User user = createUser(7L, true, false, false);

        Update update = mockUpdate(7L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.users.no_access"));
    }
} 