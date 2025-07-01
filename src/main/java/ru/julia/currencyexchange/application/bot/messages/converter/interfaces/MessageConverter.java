package ru.julia.currencyexchange.application.bot.messages.converter.interfaces;

import java.util.Map;

public interface MessageConverter {
    default String resolve(String id) {
        return resolve(id, Map.of());
    }

    String resolve(String id, Map<String, String> params);
}
