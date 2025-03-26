package ru.julia.currencyexchange.service;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.entity.CurrencyConversion;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.repository.ConversionRepository;
import ru.julia.currencyexchange.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyExchangeService {
    private final CurrencyService currencyService;
    private final ConversionRepository conversionRepository;
    private final UserRepository userRepository;

    public CurrencyExchangeService(CurrencyService currencyService, ConversionRepository conversionRepository, UserRepository userRepository) {
        this.currencyService = currencyService;
        this.conversionRepository = conversionRepository;
        this.userRepository = userRepository;
    }

    public CurrencyConversion convert(String userId, String from, String to, double amount) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        List<Currency> currencies = currencyService.getExchangeRates();

        Currency fromCurrency = currencies.stream()
                .filter(currency -> currency.getCode().equals(from))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Currency " + from + " not found"));

        Currency toCurrency = currencies.stream()
                .filter(currency -> currency.getCode().equals(to))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Currency " + to + " not found"));

        double fromRate = fromCurrency.getExchangeRate();
        double toRate = toCurrency.getExchangeRate();
        double rate = fromRate / toRate;
        double convertedAmount = amount * rate;

        // Создаем запись о конверсии
        CurrencyConversion conversion = new CurrencyConversion(
                user, from, to, amount, convertedAmount, rate
        );

        // Сохраняем результат
        conversionRepository.save(conversion);

        return conversion;
    }

    public List<CurrencyConversion> getUserHistory(String userId) {
        return conversionRepository.findConversionsByUserId(userId);
    }

    public Optional<List<CurrencyConversion>> getConversionByAmountRange(Double minAmount, Double maxAmount) {
        return conversionRepository.findConversionByAmountRange(minAmount, maxAmount);
    }

    public Optional<CurrencyConversion> findBySourceCurrencyAndTimestamp(String sourceCurrency, String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        LocalDateTime parsedTimestamp = LocalDateTime.parse(timestamp, formatter);
        return conversionRepository.findBySourceCurrencyAndTimestamp(sourceCurrency, parsedTimestamp);
    }

    // Метод для обновления курсов валют
    public List<Currency> updateCurrencyRates() {
        return currencyService.getExchangeRates();
    }
}
