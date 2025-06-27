package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.time.LocalDate;
import java.util.List;

public interface ConversionRepository extends CrudRepository<CurrencyConversion, String> {
    // Поиск по дате и пользователю
    @Query("SELECT c FROM CurrencyConversion c WHERE FUNCTION('DATE', c.timestamp) = :date AND c.user.id = :userId")
    List<CurrencyConversion> findByCurrencyDate(
            @Param("date") LocalDate date,
            @Param("userId") String userId
    );

    // Поиск конвертаций по userId
    @Query("SELECT c FROM CurrencyConversion c WHERE c.user.id = :userId")
    List<CurrencyConversion> findConversionByUserId(@Param("userId") String userId);
}