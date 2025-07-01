package ru.julia.currencyexchange.application.bot.listener;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.julia.currencyexchange.application.bot.executor.interfaces.Executor;
import ru.julia.currencyexchange.application.bot.messages.DefaultMessages;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class MessagesListenerUnitTest {
    @Mock
    private Executor executor;
    @Mock
    private DefaultMessages defaultMessages;
    @InjectMocks
    private MessagesListener listener;

    @BeforeEach
    void setUp() {
        openMocks(this);
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

    @Test
    @DisplayName("CallbackQuery: handleCallback возвращает true — executor не вызывается")
    void process_callbackQuery_handleCallbackTrue_noExecute() {
        MessagesListener spyListener = spy(listener);
        Update update = mock(Update.class);

        when(update.callbackQuery()).thenReturn(mock(CallbackQuery.class));
        doReturn(true).when(spyListener).handleCallback(update);

        spyListener.process(List.of(update));

        verify(executor, never()).execute(any());
    }

    @Test
    @DisplayName("executor.execute выбрасывает исключение — процесс не падает")
    void process_executorThrows_noCrash() {
        Update update = mock(Update.class);
        SendMessage sendMessage = mock(SendMessage.class);

        when(update.callbackQuery()).thenReturn(null);
        when(defaultMessages.sendMessage(update)).thenReturn(sendMessage);
        when(sendMessage.getParameters()).thenReturn(Map.of());

        doThrow(new RuntimeException("fail")).when(executor).execute(sendMessage);

        listener.process(List.of(update));
    }

    @Test
    @DisplayName("Пустой список update — executor не вызывается")
    void process_emptyList_noExecute() {
        listener.process(List.of());

        verify(executor, never()).execute(any());
    }

    @Test
    @DisplayName("CallbackQuery: если callbackData == null — executor не вызывается")
    void process_callbackQuery_callbackDataNull_noExecute() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.callbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.data()).thenReturn(null);

        listener.process(List.of(update));

        verify(executor, never()).execute(any());
    }
} 