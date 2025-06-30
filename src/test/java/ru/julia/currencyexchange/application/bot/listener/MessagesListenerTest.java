package ru.julia.currencyexchange.application.bot.listener;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.application.bot.executor.interfaces.Executor;
import ru.julia.currencyexchange.application.bot.messages.DefaultMessages;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class MessagesListenerTest {
    @Mock
    private Executor executor;
    @Mock
    private DefaultMessages defaultMessages;
    @InjectMocks
    private MessagesListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Обычное сообщение: вызывается sendMessage и executor.execute")
    void process_shouldCallSendMessageAndExecute() {
        Update update = mock(Update.class);
        SendMessage sendMessage = mock(SendMessage.class);
        when(update.callbackQuery()).thenReturn(null);
        when(defaultMessages.sendMessage(update)).thenReturn(sendMessage);
        when(sendMessage.getParameters()).thenReturn(Map.of());
        listener.process(List.of(update));
        verify(defaultMessages).sendMessage(update);
        verify(executor).execute(sendMessage);
    }

    @Test
    @DisplayName("Если sendMessage возвращает null, executor не вызывается")
    void process_sendMessageNull_noExecute() {
        Update update = mock(Update.class);
        when(update.callbackQuery()).thenReturn(null);
        when(defaultMessages.sendMessage(update)).thenReturn(null);
        listener.process(List.of(update));
        verify(executor, never()).execute(any());
    }
} 