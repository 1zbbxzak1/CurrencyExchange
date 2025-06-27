package ru.julia.currencyexchange.application.util;

import ru.julia.currencyexchange.application.dto.currency.CurrencyConversionResponse;
import ru.julia.currencyexchange.application.dto.currency.CurrencyResponse;
import ru.julia.currencyexchange.application.dto.user.UserResponse;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;

import java.util.stream.Collectors;

public class DtoMapper {
    public static CurrencyResponse mapToCurrencyResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getExchangeRate(),
                currency.getLastUpdated()
        );
    }

    public static CurrencyConversionResponse mapToCurrencyConversionResponse(CurrencyConversion conversion) {
        return new CurrencyConversionResponse(
                conversion.getId(),
                conversion.getUser().getId(),
                conversion.getSourceCurrency().getCode(),
                conversion.getTargetCurrency().getCode(),
                conversion.getAmount(),
                conversion.getConvertedAmount(),
                conversion.getConversionRate(),
                conversion.getTimestamp()
        );
    }

    public static UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getChatId(),
                user.getUsername(),
                user.getEmail(),
                user.isVerified(),
                user.isBanned(),
                user.getRoles().stream()
                        .map(userRole -> userRole.getRole().getRoleName())
                        .collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
} 