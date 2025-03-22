package ru.julia.currencyexchange.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.dto.CurrencyConversion;
import ru.julia.currencyexchange.repository.interfaces.CrudRepository;

import java.util.List;
import java.util.Map;

@Component
public class CurrencyExchangeRepository implements CrudRepository<CurrencyConversion, Long> {
    private final Map<Long, CurrencyConversion> conversions;
    private final IdGenerator idGenerator;

    @Autowired
    public CurrencyExchangeRepository(Map<Long, CurrencyConversion> conversions, IdGenerator idGenerator) {
        this.conversions = conversions;
        this.idGenerator = idGenerator;
    }

    @Override
    public void create(CurrencyConversion entity) {
        entity.setId(idGenerator.get());
        conversions.put(entity.getId(), entity);
    }

    @Override
    public CurrencyConversion read(Long id) {
        return conversions.get(id);
    }

    @Override
    public List<CurrencyConversion> readAll() {
        return conversions.values().stream().toList();
    }

    @Override
    public void delete(Long id) {
        conversions.remove(id);
    }

    @Override
    public void update(CurrencyConversion entity) {
        conversions.put(entity.getId(), entity);
    }
}
