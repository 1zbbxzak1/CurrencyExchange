package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.infrastructure.repository.criteria.CustomConversionRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConversionRepository extends CrudRepository<CurrencyConversion, String>, CustomConversionRepository {
    // Поиск по имени и роли
    @Query("SELECT c FROM CurrencyConversion c WHERE c.sourceCurrency = :currency AND FUNCTION('DATE', c.timestamp) = :date")
    List<CurrencyConversion> findByCurrencyCodeAndDate(@Param("currency") Currency sourceCurrency, @Param("date") LocalDate date);
}