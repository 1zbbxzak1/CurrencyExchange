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
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.SetFeeStateService;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.bot.command.SetFeeCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.SetFeeKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.SetFeeCallbackHandler;
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
class SetFeeCommandIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private SetFeeStateService setFeeStateService;
    @Autowired
    private SetFeeKeyboardBuilder keyboardBuilder;
    @Autowired
    private SetFeeCallbackHandler callbackHandler;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    private SetFeeCommand command;

    @BeforeEach
    void setUp() {
        command = new SetFeeCommand(messageConverter, userService, settingsService, setFeeStateService, keyboardBuilder, callbackHandler);
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Обычный вызов")
    void handle_admin_success() {
        User user = createUser(1L, true, false, true);

        Update update = mockUpdate(1L, "/setFee");
        setFeeStateService.clearState(1L);
        settingsService.setGlobalConversionFee(2.5);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("2.5");
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

    private Update mockUpdate(Long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(text);

        return update;
    }

    @Test
    @DisplayName("Ручной ввод комиссии, валидное значение")
    void handle_admin_manualFee_success() {
        User user = createUser(2L, true, false, true);

        Update update = mockUpdate(2L, "3.5");

        setFeeStateService.setState(2L, SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains("3.5");
        assertThat(settingsService.getGlobalConversionFeePercent()).isEqualTo(3.5);
        assertThat(setFeeStateService.getState(2L)).isEqualTo(SetFeeState.NONE);
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: отрицательное значение")
    void handle_admin_manualFee_negative() {
        User user = createUser(3L, true, false, true);

        Update update = mockUpdate(3L, "-1");

        setFeeStateService.setState(3L, SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.invalid_value"));
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: больше 100")
    void handle_admin_manualFee_tooBig() {
        User user = createUser(4L, true, false, true);

        Update update = mockUpdate(4L, "101");

        setFeeStateService.setState(4L, SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.invalid_value"));
    }

    @Test
    @DisplayName("Невалидный ввод комиссии: не число")
    void handle_admin_manualFee_notNumber() {
        User user = createUser(5L, true, false, true);

        Update update = mockUpdate(5L, "abc");

        setFeeStateService.setState(5L, SetFeeState.WAITING_MANUAL_FEE);

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.invalid_value"));
    }

    @Test
    @DisplayName("Нет доступа: не admin")
    void handle_notAdmin() {
        User user = createUser(6L, false, false, true);

        Update update = mockUpdate(6L, "/setFee");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.no_access"));
    }

    @Test
    @DisplayName("Нет доступа: удалён")
    void handle_deleted() {
        User user = createUser(7L, true, true, true);

        Update update = mockUpdate(7L, "/setFee");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.no_access"));
    }

    @Test
    @DisplayName("Нет доступа: не верифицирован")
    void handle_notVerified() {
        User user = createUser(8L, true, false, false);

        Update update = mockUpdate(8L, "/setFee");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).asString().contains(messageConverter.resolve("command.setFee.no_access"));
    }
} 