package ru.julia.currencyexchange.service;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.dto.CurrencyConversion;
import ru.julia.currencyexchange.repository.CurrencyExchangeRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CurrencyExchangeService {
    private final CurrencyService currencyService;
    private final CurrencyExchangeRepository repository;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public CurrencyExchangeService(CurrencyService currencyService, CurrencyExchangeRepository repository) {
        this.currencyService = currencyService;
        this.repository = repository;
    }

    public double convert(String from, String to, double amount) {
        double rate = currencyService.getExchangeRates().get(from) / currencyService.getExchangeRates().get(to);
        double convertedAmount = amount * rate;

        // Сохраняем в репозиторий
        CurrencyConversion conversion = new CurrencyConversion(
                idGenerator.getAndIncrement(), from, to, amount, convertedAmount, rate
        );
        repository.create(conversion);

        return convertedAmount;
    }

    public List<CurrencyConversion> getConversionHistory() {
        return repository.readAll();
    }
}
