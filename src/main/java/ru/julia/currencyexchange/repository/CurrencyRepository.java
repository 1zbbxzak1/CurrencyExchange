package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.Currency;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, String> {
}
