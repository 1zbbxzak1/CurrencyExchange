package ru.julia.currencyexchange.application.bot.executor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.executor.interfaces.Executor;

@Component
public class RequestExecutor implements Executor {
    private final TelegramBot bot;

    public RequestExecutor(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        if (bot == null) {
            throw new IllegalStateException("Bot not initialized");
        }

        R response = bot.execute(request);

        return response;
    }
}
