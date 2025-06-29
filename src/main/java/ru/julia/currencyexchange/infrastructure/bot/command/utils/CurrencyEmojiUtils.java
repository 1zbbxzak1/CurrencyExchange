package ru.julia.currencyexchange.infrastructure.bot.command.utils;

import org.springframework.stereotype.Component;

@Component
public class CurrencyEmojiUtils {
    
    public String getCurrencyEmoji(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> "🇺🇸";
            case "EUR" -> "🇪🇺";
            case "GBP" -> "🇬🇧";
            case "JPY" -> "🇯🇵";
            case "CNY" -> "🇨🇳";
            case "CHF" -> "🇨🇭";
            case "CAD" -> "🇨🇦";
            case "AUD" -> "🇦🇺";
            case "NZD" -> "🇳🇿";
            case "SEK" -> "🇸🇪";
            case "NOK" -> "🇳🇴";
            case "DKK" -> "🇩🇰";
            case "PLN" -> "🇵🇱";
            case "CZK" -> "🇨🇿";
            case "HUF" -> "🇭🇺";
            case "BGN" -> "🇧🇬";
            case "RON" -> "🇷🇴";
            case "HRK" -> "🇭🇷";
            case "TRY" -> "🇹🇷";
            case "BRL" -> "🇧🇷";
            case "MXN" -> "🇲🇽";
            case "INR" -> "🇮🇳";
            case "KRW" -> "🇰🇷";
            case "SGD" -> "🇸🇬";
            case "HKD" -> "🇭🇰";
            case "THB" -> "🇹🇭";
            case "MYR" -> "🇲🇾";
            case "IDR" -> "🇮🇩";
            case "PHP" -> "🇵🇭";
            case "VND" -> "🇻🇳";
            case "RUB" -> "🇷🇺";
            default -> "🏳️";
        };
    }
} 