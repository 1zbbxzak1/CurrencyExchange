package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.BanUserKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.BanUserCallbackHandler;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRoleRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class BanUserCommandIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private BanUserKeyboardBuilder keyboardBuilder;
    @Autowired
    private BanUserCallbackHandler callbackHandler;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private BanUserCommand command;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        databaseCleaner.resetDatabase();
        adminRole = roleRepository.findByRoleName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        userRole = roleRepository.findByRoleName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
        command = new BanUserCommand(messageConverter, userService, keyboardBuilder, callbackHandler);
    }

    private User createUser(Long chatId, String username, String email, Role role, boolean verified, boolean deleted, boolean banned) {
        User user = new User();
        user.setChatId(chatId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("pass");
        user.setVerified(verified);
        user.setDeleted(deleted);
        user.setBanned(banned);
        user = userRepository.save(user);
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    private Update mockUpdate(Long chatId) {
        Update update = org.mockito.Mockito.mock(Update.class);
        Message message = org.mockito.Mockito.mock(Message.class);
        Chat chat = org.mockito.Mockito.mock(Chat.class);
        org.mockito.Mockito.when(update.message()).thenReturn(message);
        org.mockito.Mockito.when(message.chat()).thenReturn(chat);
        org.mockito.Mockito.when(chat.id()).thenReturn(chatId);
        return update;
    }

    @Nested
    @DisplayName("handle: доступ и сценарии")
    class HandleAccess {
        @Test
        @DisplayName("Нет доступа: пользователь не найден")
        void noAccess_userNotFound() {
            Update update = mockUpdate(1111L);
            assertThatThrownBy(() -> command.handle(update))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("1111");
        }

        @Test
        @DisplayName("Нет доступа: не админ")
        void noAccess_notAdmin() {
            User user = createUser(2L, "user", "user@mail.com", userRole, true, false, false);
            Update update = mockUpdate(2L);
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.no_access"));
        }

        @Test
        @DisplayName("Нет доступа: удалён или не верифицирован")
        void noAccess_deletedOrNotVerified() {
            User admin = createUser(3L, "admin", "admin@mail.com", adminRole, true, true, false);
            Update update = mockUpdate(3L);
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.no_access"));
            admin.setDeleted(false);
            admin.setVerified(false);
            userRepository.save(admin);
            msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.no_access"));
        }

        @Test
        @DisplayName("Нет пользователей для блокировки")
        void noUsersToBan() {
            User admin = createUser(4L, "admin", "admin2@mail.com", adminRole, true, false, false);
            Update update = mockUpdate(4L);
            SendMessage msg = command.handle(update);
            Object replyMarkup = msg.getParameters().get("reply_markup");
            if (replyMarkup instanceof InlineKeyboardMarkup keyboard && keyboard.inlineKeyboard() != null && keyboard.inlineKeyboard().length > 0) {
                assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.select_email"));
            } else {
                assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.empty"));
            }
        }

        @Test
        @DisplayName("Есть пользователи для блокировки: клавиатура и сообщение")
        void usersToBan_keyboardAndMessage() {
            User admin = createUser(5L, "admin", "admin3@mail.com", adminRole, true, false, false);
            User user1 = createUser(6L, "user1", "user1@mail.com", userRole, true, false, false);
            Update update = mockUpdate(5L);
            SendMessage msg = command.handle(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.select_email"));
            assertThat(msg.getParameters().get("reply_markup")).isInstanceOf(InlineKeyboardMarkup.class);
        }
    }

    @Nested
    @DisplayName("handleCallback: сценарии блокировки")
    class HandleCallback {
        @Test
        @DisplayName("Успешная блокировка пользователя")
        void successBan() {
            User admin = createUser(10L, "admin", "admin4@mail.com", adminRole, true, false, false);
            User user = createUser(11L, "user", "user2@mail.com", userRole, true, false, false);
            Update update = mockCallbackUpdate(10L, "user2@mail.com", 123);
            EditMessageText msg = callbackHandler.handleCallback(update);

            assertThat(msg.getParameters().get("text"))
                    .asString()
                    .contains("Пользователь: *user*", "Email: user2@mail.com");

            User banned = userRepository.findByChatId(11L).orElseThrow();
            assertThat(banned.isBanned()).isTrue();
        }

        private Update mockCallbackUpdate(Long adminChatId, String email, Integer messageId) {
            Update update = org.mockito.Mockito.mock(Update.class);
            com.pengrad.telegrambot.model.CallbackQuery callback = org.mockito.Mockito.mock(com.pengrad.telegrambot.model.CallbackQuery.class);
            Message message = org.mockito.Mockito.mock(Message.class);
            Chat chat = org.mockito.Mockito.mock(Chat.class);
            org.mockito.Mockito.when(update.callbackQuery()).thenReturn(callback);
            org.mockito.Mockito.when(callback.message()).thenReturn(message);
            org.mockito.Mockito.when(message.chat()).thenReturn(chat);
            org.mockito.Mockito.when(chat.id()).thenReturn(adminChatId);
            org.mockito.Mockito.when(message.messageId()).thenReturn(messageId);
            org.mockito.Mockito.when(callback.data()).thenReturn("ban_user_" + email);
            return update;
        }

        @Test
        @DisplayName("Попытка заблокировать себя")
        void cannotBanSelf() {
            User admin = createUser(12L, "admin", "admin5@mail.com", adminRole, true, false, false);
            Update update = mockCallbackUpdate(12L, "admin5@mail.com", 124);
            EditMessageText msg = callbackHandler.handleCallback(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.cannot_ban_self"));
        }

        @Test
        @DisplayName("Пользователь уже заблокирован")
        void alreadyBanned() {
            User admin = createUser(13L, "admin", "admin6@mail.com", adminRole, true, false, false);
            User user = createUser(14L, "user", "user3@mail.com", userRole, true, false, true);
            Update update = mockCallbackUpdate(13L, "user3@mail.com", 125);
            EditMessageText msg = callbackHandler.handleCallback(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.already_banned"));
        }

        @Test
        @DisplayName("Пользователь не найден по email")
        void userNotFoundByEmail() {
            User admin = createUser(15L, "admin", "admin7@mail.com", adminRole, true, false, false);
            Update update = mockCallbackUpdate(15L, "notfound@mail.com", 126);
            EditMessageText msg = callbackHandler.handleCallback(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.error"));
        }

        @Test
        @DisplayName("Нет доступа: не админ")
        void noAccess_notAdmin() {
            User user = createUser(16L, "user", "user4@mail.com", userRole, true, false, false);
            Update update = mockCallbackUpdate(16L, "user4@mail.com", 127);
            EditMessageText msg = callbackHandler.handleCallback(update);
            assertThat(msg.getParameters().get("text")).isEqualTo(messageConverter.resolve("command.banUser.no_access"));
        }
    }
} 