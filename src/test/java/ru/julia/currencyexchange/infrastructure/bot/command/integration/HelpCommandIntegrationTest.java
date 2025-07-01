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
import ru.julia.currencyexchange.infrastructure.bot.command.HelpCommand;
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
class HelpCommandIntegrationTest extends IntegrationTestBase {
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

    private HelpCommand command;

    @BeforeEach
    void setUp() {
        command = new HelpCommand(messageConverter, userService);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Не зарегистрирован")
    void handle_notRegistered() {
        Update update = mockUpdate(1L);
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.unregistered_help_message"));
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
    @DisplayName("Забанен")
    void handle_banned() {
        User user = createUser(2L, true, false, true);
        assignRole(user, "ROLE_USER");
        Update update = mockUpdate(2L);
        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.banned_message"));
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

    private void assignRole(User user, String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    @Test
    @DisplayName("Удалён")
    void handle_deleted() {
        User user = createUser(3L, false, true, true);
        assignRole(user, "ROLE_USER");

        Update update = mockUpdate(3L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.deleted_help_message"));
    }

    @Test
    @DisplayName("Верифицированный admin")
    void handle_admin() {
        User user = createUser(4L, false, false, true);
        assignRole(user, "ROLE_ADMIN");

        Update update = mockUpdate(4L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.admin_help_message"));
    }

    @Test
    @DisplayName("Верифицированный user")
    void handle_user() {
        User user = createUser(5L, false, false, true);
        assignRole(user, "ROLE_USER");

        Update update = mockUpdate(5L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.user_help_message"));
    }

    @Test
    @DisplayName("Не верифицирован")
    void handle_unverified() {
        User user = createUser(6L, false, false, false);
        assignRole(user, "ROLE_USER");

        Update update = mockUpdate(6L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.unverified_help_message"));
    }

    @Test
    @DisplayName("Ошибка — пользователь удалён после создания")
    void handle_error() {
        User user = createUser(7L, false, false, true);
        assignRole(user, "ROLE_USER");
        userRepository.delete(user);

        Update update = mockUpdate(7L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.help.unregistered_help_message"));
    }
} 