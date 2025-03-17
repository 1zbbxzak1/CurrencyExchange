package ru.julia.currencyexchange.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.dto.CurrencyConversion;
import ru.julia.currencyexchange.repository.interfaces.CrudRepository;

import java.util.List;

@Component
public class CurrencyExchangeRepository implements CrudRepository<CurrencyConversion, Long> {
    private final List<CurrencyConversion> conversions;

    @Autowired
    public CurrencyExchangeRepository(List<CurrencyConversion> conversions) {
        this.conversions = conversions;
    }

    @Override
    public void create(CurrencyConversion entity) {
        conversions.add(entity);
    }

    @Override
    public CurrencyConversion read(Long id) {
        return conversions.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<CurrencyConversion> readAll() {
        return conversions;
    }
}
