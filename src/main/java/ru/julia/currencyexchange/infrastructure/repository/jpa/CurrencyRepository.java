package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.julia.currencyexchange.domain.model.Currency;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
    Optional<Currency> findByCode(String code);
}
