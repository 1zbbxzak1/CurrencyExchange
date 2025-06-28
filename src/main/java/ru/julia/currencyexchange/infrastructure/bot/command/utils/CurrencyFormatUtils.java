package ru.julia.currencyexchange.infrastructure.bot.command.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CurrencyFormatUtils {
    
    public String formatExchangeRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ONE) >= 0) {
            return rate.setScale(4, RoundingMode.HALF_UP).toString();
        } else {
            return rate.setScale(6, RoundingMode.HALF_UP).toString();
        }
    }
} 