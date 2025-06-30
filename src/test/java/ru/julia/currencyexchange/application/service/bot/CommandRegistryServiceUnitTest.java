package ru.julia.currencyexchange.application.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.infrastructure.bot.command.interfaces.BotCommandHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CommandRegistryServiceUnitTest {
    @Mock
    private TelegramBot bot;
    @Mock
    private BotCommandHandler handler1;
    @Mock
    private BotCommandHandler handler2;
    private CommandRegistryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Успешная регистрация команд")
    void init_success() {
        when(handler1.toBotCommand()).thenReturn(new BotCommand("/start", "desc1"));
        when(handler2.toBotCommand()).thenReturn(new BotCommand("/help", "desc2"));
        service = new CommandRegistryService(bot, List.of(handler1, handler2));
        service.init();
        ArgumentCaptor<SetMyCommands> captor = ArgumentCaptor.forClass(SetMyCommands.class);
        verify(bot).execute(captor.capture());
        SetMyCommands setCmd = captor.getValue();
        BotCommand[] commands = (BotCommand[]) setCmd.getParameters().get("commands");
        assertThat(commands).extracting(BotCommand::command).containsExactly("/start", "/help");
    }

    @Test
    @DisplayName("Пустой список команд — execute вызывается с пустым массивом")
    void init_emptyList() {
        service = new CommandRegistryService(bot, Collections.emptyList());
        service.init();
        ArgumentCaptor<SetMyCommands> captor = ArgumentCaptor.forClass(SetMyCommands.class);
        verify(bot).execute(captor.capture());
        SetMyCommands setCmd = captor.getValue();
        BotCommand[] commands = (BotCommand[]) setCmd.getParameters().get("commands");
        assertThat(commands).isEmpty();
    }

    @Test
    @DisplayName("Бот выбрасывает исключение при execute — выбрасывается наружу")
    void init_botThrowsException() {
        when(handler1.toBotCommand()).thenReturn(new BotCommand("/fail", "desc"));
        service = new CommandRegistryService(bot, List.of(handler1));
        doThrow(new RuntimeException("fail")).when(bot).execute(any(SetMyCommands.class));
        assertThrows(RuntimeException.class, () -> service.init());
    }

    @Test
    @DisplayName("Команда возвращает некорректный BotCommand (null)")
    void init_commandReturnsNull() {
        when(handler1.toBotCommand()).thenReturn(null);
        service = new CommandRegistryService(bot, List.of(handler1));
        service.init();
        ArgumentCaptor<SetMyCommands> captor = ArgumentCaptor.forClass(SetMyCommands.class);
        verify(bot).execute(captor.capture());
        SetMyCommands setCmd = captor.getValue();
        BotCommand[] commands = (BotCommand[]) setCmd.getParameters().get("commands");
        assertThat(commands).containsExactly((BotCommand) null);
    }

    @Test
    @DisplayName("Команды с одинаковыми именами")
    void init_duplicateCommands() {
        when(handler1.toBotCommand()).thenReturn(new BotCommand("/same", "desc1"));
        when(handler2.toBotCommand()).thenReturn(new BotCommand("/same", "desc2"));
        service = new CommandRegistryService(bot, Arrays.asList(handler1, handler2));
        service.init();
        ArgumentCaptor<SetMyCommands> captor = ArgumentCaptor.forClass(SetMyCommands.class);
        verify(bot).execute(captor.capture());
        SetMyCommands setCmd = captor.getValue();
        BotCommand[] commands = (BotCommand[]) setCmd.getParameters().get("commands");
        assertThat(commands).extracting(BotCommand::command).containsExactly("/same", "/same");
    }

    @Test
    @DisplayName("Список команд содержит null — выбрасывается NullPointerException")
    void init_commandsListContainsNull() {
        service = new CommandRegistryService(bot, Arrays.asList(handler1, null));
        when(handler1.toBotCommand()).thenReturn(new BotCommand("/a", "desc"));
        assertThrows(NullPointerException.class, () -> service.init());
    }
} 