package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.Currency;

import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency, String> {
    Optional<Currency> findByCode(String code);
}
