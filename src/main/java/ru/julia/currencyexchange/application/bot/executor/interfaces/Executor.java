package ru.julia.currencyexchange.application.bot.executor.interfaces;

import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;

public interface Executor {
    <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request);
}