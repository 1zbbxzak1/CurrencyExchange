package ru.julia.currencyexchange.application.bot.settings;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InOrder;
import ru.julia.currencyexchange.application.bot.listener.MessagesListener;
import ru.julia.currencyexchange.application.service.bot.CommandRegistryService;

import static org.mockito.Mockito.*;

class BotInitTest {
    @Mock
    private TelegramBot bot;
    @Mock
    private MessagesListener messagesListener;
    @Mock
    private CommandRegistryService commandRegistryService;
    private BotInit botInit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        botInit = new BotInit(bot, messagesListener, commandRegistryService);
    }

    @Test
    @DisplayName("start вызывает init и setUpdatesListener в правильном порядке и только один раз")
    void start_callsInitAndSetUpdatesListener_inOrder() {
        botInit.start();
        InOrder inOrder = inOrder(commandRegistryService, bot);
        inOrder.verify(commandRegistryService).init();
        inOrder.verify(bot).setUpdatesListener(messagesListener);
        verify(bot, times(1)).setUpdatesListener(messagesListener);
        verify(commandRegistryService, times(1)).init();
        verifyNoMoreInteractions(bot, commandRegistryService);
    }

    @Test
    @DisplayName("close вызывает shutdown и больше ничего")
    void close_callsOnlyShutdown() {
        botInit.close();
        verify(bot, times(1)).shutdown();
        verifyNoMoreInteractions(bot, messagesListener, commandRegistryService);
    }
} 