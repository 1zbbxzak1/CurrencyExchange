package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SetFeeStateService {
    private final Map<Long, SetFeeState> userStates = new ConcurrentHashMap<>();

    public SetFeeState getState(Long chatId) {
        return userStates.getOrDefault(chatId, SetFeeState.NONE);
    }

    public void setState(Long chatId, SetFeeState state) {
        userStates.put(chatId, state);
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }
} 