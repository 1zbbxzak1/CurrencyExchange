package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.settings.RegistrationData;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationStateService {
    private final Map<Long, RegistrationState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, RegistrationData> userData = new ConcurrentHashMap<>();

    public RegistrationState getState(Long chatId) {
        return userStates.getOrDefault(chatId, RegistrationState.NONE);
    }

    public void setState(Long chatId, RegistrationState state) {
        userStates.put(chatId, state);
    }

    public void clearData(Long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    public void setEmail(Long chatId, String email) {
        getData(chatId).setEmail(email);
    }

    public RegistrationData getData(Long chatId) {
        return userData.computeIfAbsent(chatId, k -> new RegistrationData());
    }

    public void setPassword(Long chatId, String password) {
        getData(chatId).setPassword(password);
    }

    public void setVerificationCode(Long chatId, String code) {
        getData(chatId).setVerificationCode(code);
    }
} 