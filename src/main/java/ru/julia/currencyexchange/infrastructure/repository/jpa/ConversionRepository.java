package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.time.LocalDate;
import java.util.List;

public interface ConversionRepository extends CrudRepository<CurrencyConversion, String> {
    // Поиск по имени и роли
    @Query("SELECT c FROM CurrencyConversion c WHERE c.sourceCurrency = :currency AND FUNCTION('DATE', c.timestamp) = :date")
    List<CurrencyConversion> findByCurrencyCodeAndDate(
            @Param("currency") Currency sourceCurrency,
            @Param("date") LocalDate date
    );

    // Поиск конвертаций по userId
    @Query("SELECT c FROM CurrencyConversion c WHERE c.user.id = :userId")
    List<CurrencyConversion> findConversionByUserId(@Param("userId") String userId);
}