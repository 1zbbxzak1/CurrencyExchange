package ru.julia.currencyexchange.application.bot.messages;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.infrastructure.bot.command.RegisterCommand;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DefaultMessagesTest {
    @Mock
    private MessageConverter messageConverter;
    @Mock
    private RegistrationStateService registrationStateService;
    @Mock
    private UserService userService;
    @Mock
    private RegisterCommand registerCommand;
    @Mock
    private BotCommandHandler otherCommand;
    private DefaultMessages defaultMessages;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultMessages = new DefaultMessages(
                messageConverter,
                registrationStateService,
                userService,
                List.of(registerCommand, otherCommand),
                null, null, null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("Если update.message() == null, возвращается null")
    void sendMessage_messageNull_returnsNull() {
        Update update = mock(Update.class);
        when(update.message()).thenReturn(null);
        assertThat(defaultMessages.sendMessage(update)).isNull();
    }

    @Test
    @DisplayName("Если пользователь в регистрации — вызывается RegisterCommand")
    void sendMessage_registrationState_callsRegisterCommand() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(registrationStateService.getState(1L)).thenReturn(RegistrationState.WAITING_EMAIL);
        SendMessage expected = new SendMessage(1L, "reg");
        when(registerCommand.handle(update)).thenReturn(expected);
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Команда не доступна — возвращается access denied")
    void sendMessage_commandNotAccessible_returnsAccessDenied() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(2L);
        when(message.text()).thenReturn("/admin");
        when(registrationStateService.getState(2L)).thenReturn(RegistrationState.NONE);
        when(otherCommand.matches(update)).thenReturn(true);
        when(userService.existsByChatId(2L)).thenReturn(false);
        when(otherCommand.isAccessible(null)).thenReturn(false);
        when(messageConverter.resolve("command.access_denied")).thenReturn("Нет доступа");
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result.getParameters().toString()).contains("Нет доступа");
    }

    @Test
    @DisplayName("Неизвестная команда — возвращается unknown command")
    void sendMessage_unknownCommand_returnsUnknownCommand() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(3L);
        when(message.text()).thenReturn("/unknown");
        when(registrationStateService.getState(3L)).thenReturn(RegistrationState.NONE);
        when(otherCommand.matches(update)).thenReturn(false);
        when(messageConverter.resolve(eq("message.unknown_command"), any())).thenReturn("Неизвестная команда");
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result.getParameters().toString()).contains("Неизвестная команда");
    }

    @Test
    @DisplayName("Команда доступна и matches — вызывается handle")
    void sendMessage_commandAccessible_callsHandle() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(4L);
        when(message.text()).thenReturn("/start");
        when(registrationStateService.getState(4L)).thenReturn(RegistrationState.NONE);
        when(otherCommand.matches(update)).thenReturn(true);
        when(userService.existsByChatId(4L)).thenReturn(false);
        when(otherCommand.isAccessible(null)).thenReturn(true);
        SendMessage expected = new SendMessage(4L, "Добро пожаловать!");
        when(otherCommand.handle(update)).thenReturn(expected);
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("resolve('command.access_denied') возвращает пустую строку — возвращается дефолтное сообщение")
    void sendMessage_accessDenied_emptyMessage_returnsDefault() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/admin");
        when(registrationStateService.getState(5L)).thenReturn(RegistrationState.NONE);
        when(otherCommand.matches(update)).thenReturn(true);
        when(userService.existsByChatId(5L)).thenReturn(false);
        when(otherCommand.isAccessible(null)).thenReturn(false);
        when(messageConverter.resolve("command.access_denied")).thenReturn("");
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result.getParameters().toString()).contains("У вас нет доступа к этой команде");
    }

    @Test
    @DisplayName("resolve('message.unknown_command') возвращает пустую строку — возвращается дефолтное сообщение")
    void sendMessage_unknownCommand_emptyMessage_returnsDefault() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(6L);
        when(message.text()).thenReturn("/unknown");
        when(registrationStateService.getState(6L)).thenReturn(RegistrationState.NONE);
        when(otherCommand.matches(update)).thenReturn(false);
        when(messageConverter.resolve(eq("message.unknown_command"), any())).thenReturn("");
        SendMessage result = defaultMessages.sendMessage(update);
        assertThat(result.getParameters().toString()).contains("Неизвестная команда. Посмотрите список доступных команд, написав /help.");
    }
} 