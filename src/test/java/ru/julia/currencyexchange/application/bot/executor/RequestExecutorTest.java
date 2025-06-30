package ru.julia.currencyexchange.application.bot.executor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RequestExecutorTest {
    @Mock
    private TelegramBot bot;
    @InjectMocks
    private RequestExecutor executor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Успешный вызов execute")
    void execute_shouldCallBotExecute() {
        BaseRequest request = mock(BaseRequest.class);
        BaseResponse response = mock(BaseResponse.class);
        when(bot.execute(request)).thenReturn(response);

        BaseResponse result = executor.execute(request);

        verify(bot).execute(request);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("Bot не инициализирован")
    void execute_shouldThrowIfBotIsNull() {
        RequestExecutor nullExecutor = new RequestExecutor(null);
        BaseRequest request = mock(BaseRequest.class);

        assertThrows(IllegalStateException.class, () -> nullExecutor.execute(request));
    }
} 