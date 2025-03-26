package ru.julia.currencyexchange.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.CurrencyConversion;
import ru.julia.currencyexchange.repository.custom.CustomConversionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversionRepository extends CrudRepository<CurrencyConversion, String>, CustomConversionRepository {
    // Поиск по имени и роли
    Optional<CurrencyConversion> findBySourceCurrencyAndTimestamp(String sourceCurrency, LocalDateTime timestamp);

    // Поиск всех конверсий по userId
    @Query("SELECT c FROM CurrencyConversion c WHERE c.user.id = :userId")
    List<CurrencyConversion> findConversionsByUserId(String userId);
}