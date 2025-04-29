package ru.julia.currencyexchange.infrastructure.repository.criteria;

import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.util.List;
import java.util.Optional;

public interface CustomConversionRepository {
    // Поиск конверсий по userId
    List<CurrencyConversion> findConversionByUserId(String userId);

    // Поиск конверсий по диапазону суммы
    Optional<List<CurrencyConversion>> findConversionByAmountRange(Double minAmount, Double maxAmount);
}
