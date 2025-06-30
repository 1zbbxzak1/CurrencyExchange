package ru.julia.currencyexchange.infrastructure.bot.command.unit;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.DeleteAccountCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.DeleteAccountKeyboardBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAccountCommandUnitTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private DeleteAccountKeyboardBuilder keyboardBuilder;
    @Mock
    private DeleteAccountValidationService validationService;
    @InjectMocks
    private DeleteAccountCommand command;

    @Test
    @DisplayName("Подтверждение удаления")
    void handle_validUser() {
        Long chatId = 1L;

        Update update = mockUpdate(chatId);

        User user = new User();
        when(validationService.validateUserForDeletion(chatId)).thenReturn(ValidationResult.success(user));

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(keyboardBuilder.buildDeleteAccountKeyboard()).thenReturn(keyboard);
        when(messageConverter.resolve("command.deleteAccount.confirmation")).thenReturn("CONFIRM?");

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("CONFIRM?");
        assertThat(msg.getParameters().get("reply_markup")).isEqualTo(keyboard);
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
        Long chatId = 2L;

        Update update = mockUpdate(chatId);

        when(validationService.validateUserForDeletion(chatId)).thenReturn(ValidationResult.error("NOT_REGISTERED"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("NOT_REGISTERED");
    }

    @Test
    @DisplayName("Забанен")
    void handle_banned() {
        Long chatId = 3L;

        Update update = mockUpdate(chatId);

        when(validationService.validateUserForDeletion(chatId)).thenReturn(ValidationResult.error("BANNED"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("BANNED");
    }

    @Test
    @DisplayName("Уже удалён")
    void handle_alreadyDeleted() {
        Long chatId = 4L;

        Update update = mockUpdate(chatId);

        when(validationService.validateUserForDeletion(chatId)).thenReturn(ValidationResult.error("ALREADY_DELETED"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ALREADY_DELETED");
    }

    @Test
    @DisplayName("Ошибка сервиса")
    void handle_serviceError() {
        Long chatId = 5L;

        Update update = mockUpdate(chatId);

        when(validationService.validateUserForDeletion(chatId)).thenReturn(ValidationResult.error("ERROR"));

        SendMessage msg = command.handle(update);
        assertThat(msg.getParameters().get("text")).isEqualTo("ERROR");
    }

    @Test
    @DisplayName("Проверка getCommand и getDescription")
    void metaMethods() {
        assertThat(command.getCommand()).isEqualTo("/deleteAccount");
        assertThat(command.getDescription()).isEqualTo("command.deleteAccount.description");
    }

    @Test
    @DisplayName("Проверка isAccessible — true")
    void isAccessible_true() {
        User user = new User();
        user.setVerified(true);
        user.setBanned(false);
        user.setDeleted(false);

        assertThat(command.isAccessible(user)).isTrue();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (null)")
    void isAccessible_null() {
        assertThat(command.isAccessible(null)).isFalse();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (не верифицирован)")
    void isAccessible_notVerified() {
        User user = new User();
        user.setVerified(false);
        user.setBanned(false);
        user.setDeleted(false);

        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (забанен)")
    void isAccessible_banned() {
        User user = new User();
        user.setVerified(true);
        user.setBanned(true);
        user.setDeleted(false);

        assertThat(command.isAccessible(user)).isFalse();
    }

    @Test
    @DisplayName("Проверка isAccessible — false (удалён)")
    void isAccessible_deleted() {
        User user = new User();
        user.setVerified(true);
        user.setBanned(false);
        user.setDeleted(true);

        assertThat(command.isAccessible(user)).isFalse();
    }
} 