package ru.julia.currencyexchange.application.util;

import ru.julia.currencyexchange.application.exceptions.InvalidParameterException;

public class ValidationUtil {
    
    public static void validateNotEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidParameterException(parameterName + " не может быть пустым");
        }
    }
    
    public static void validateNotNull(Object value, String parameterName) {
        if (value == null) {
            throw new InvalidParameterException(parameterName + " не может быть пустым");
        }
    }
    
    public static void validateUserId(String userId) {
        validateNotEmpty(userId, "ID пользователя");
    }
    
    public static void validateCurrencyCode(String currencyCode) {
        validateNotEmpty(currencyCode, "Код валюты");
    }
    
    public static void validateTimestamp(String timestamp) {
        validateNotEmpty(timestamp, "Дата");
    }
} 