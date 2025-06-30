package ru.julia.currencyexchange.infrastructure.bot.command;

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
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class StartCommandIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private StartCommand command;

    @BeforeEach
    void setUp() {
        databaseCleaner.resetDatabase();
        command = new StartCommand(messageConverter, userService);
    }

    @Test
    @DisplayName("Новый пользователь")
    void newUser() {
        Update update = mockUpdate(1L, "user", "Имя");
        SendMessage msg = command.handle(update);
        
        assertThat(msg.getParameters().get("text")).isEqualTo(
                messageConverter.resolve("command.start.start_message", Map.of("user_name", "Имя")));
    }

    private Update mockUpdate(Long chatId, String username, String firstName) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(chat.username()).thenReturn(username);
        when(chat.firstName()).thenReturn(firstName);

        return update;
    }

    @Test
    @DisplayName("Верифицированный пользователь")
    void verifiedUser() {
        createUser(2L, "user2", "Имя2", true, false, false);

        Update update = mockUpdate(2L, "user2", "Имя2");
        SendMessage msg = command.handle(update);

        assertThat(msg.getParameters().get("text")).isEqualTo(
                messageConverter.resolve("command.start.welcome_back_message", Map.of("user_name", "Имя2")));
    }

    private User createUser(Long chatId, String username,
                            String firstName, boolean verified,
                            boolean deleted, boolean banned) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername(username);
        user.setEmail(username + "@mail.com");
        user.setPassword("pass");
        user.setVerified(verified);
        user.setDeleted(deleted);
        user.setBanned(banned);

        return userRepository.save(user);
    }

    @Test
    @DisplayName("Пользователь не верифицирован")
    void notVerifiedUser() {
        createUser(3L, "user3", "Имя3", false, false, false);

        Update update = mockUpdate(3L, "user3", "Имя3");
        SendMessage msg = command.handle(update);

        assertThat(msg.getParameters().get("text")).isEqualTo(
                messageConverter.resolve("command.start.not_verified_message"));
    }

    @Test
    @DisplayName("Пользователь забанен")
    void bannedUser() {
        createUser(4L, "user4", "Имя4", true, false, true);

        Update update = mockUpdate(4L, "user4", "Имя4");
        SendMessage msg = command.handle(update);

        assertThat(msg.getParameters().get("text")).isEqualTo(
                messageConverter.resolve("command.start.banned_message"));
    }

    @Test
    @DisplayName("Пользователь удалён")
    void deletedUser() {
        createUser(5L, "user5", "Имя5", true, true, false);

        Update update = mockUpdate(5L, "user5", "Имя5");
        SendMessage msg = command.handle(update);

        assertThat(msg.getParameters().get("text")).isEqualTo(
                messageConverter.resolve("command.start.deleted_message"));
    }
} 