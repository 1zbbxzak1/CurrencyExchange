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
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.DeleteAccountCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.DeleteAccountKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfile
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DeleteAccountCommandIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private DeleteAccountKeyboardBuilder keyboardBuilder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private DeleteAccountValidationService deleteAccountValidationService;

    private DeleteAccountCommand command;

    @BeforeEach
    void setUp() {
        command = new DeleteAccountCommand(messageConverter, userService,
                keyboardBuilder, deleteAccountValidationService);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Подтверждение удаления")
    void handle_validUser() {
        User user = createUser(1L, false, false, true);

        Update update = mockUpdate(1L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.deleteAccount.confirmation"));
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
    @DisplayName("Не зарегистрирован")
    void handle_notRegistered() {
        Update update = mockUpdate(2L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.deleteAccount.not_registered"));
    }

    @Test
    @DisplayName("Забанен")
    void handle_banned() {
        createUser(3L, true, false, true);

        Update update = mockUpdate(3L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.deleteAccount.banned"));
    }

    @Test
    @DisplayName("Уже удалён")
    void handle_alreadyDeleted() {
        createUser(4L, false, true, true);

        Update update = mockUpdate(4L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.deleteAccount.already_deleted"));
    }

    @Test
    @DisplayName("Ошибка (сломанный пользователь)")
    void handle_serviceError() {
        createUser(5L, false, false, false);

        Update update = mockUpdate(5L);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.deleteAccount.confirmation"));
    }
} 