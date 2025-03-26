package ru.julia.currencyexchange.service;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.entity.CurrencyConversion;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.repository.ConversionRepository;
import ru.julia.currencyexchange.repository.CurrencyRepository;
import ru.julia.currencyexchange.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyExchangeService {
    private final CurrencyService currencyService;
    private final CurrencyRepository currencyRepository;
    private final ConversionRepository conversionRepository;
    private final UserRepository userRepository;

    public CurrencyExchangeService(CurrencyService currencyService, CurrencyRepository currencyRepository, ConversionRepository conversionRepository, UserRepository userRepository) {
        this.currencyService = currencyService;
        this.currencyRepository = currencyRepository;
        this.conversionRepository = conversionRepository;
        this.userRepository = userRepository;
    }

    public CurrencyConversion convert(String userId, String from, String to, BigDecimal amount) {
        currencyService.updateExchangeRates();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        Currency fromCurrency = currencyRepository.findByCode(from)
                .orElseThrow(() -> new IllegalArgumentException("Currency " + from + " not found"));

        Currency toCurrency = currencyRepository.findByCode(to)
                .orElseThrow(() -> new IllegalArgumentException("Currency " + to + " not found"));

        BigDecimal rate = fromCurrency.getExchangeRate().divide(toCurrency.getExchangeRate(), 6, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amount.multiply(rate);

        CurrencyConversion conversion = new CurrencyConversion(user, fromCurrency, toCurrency, amount, convertedAmount, rate);
        return conversionRepository.save(conversion);
    }

    public List<CurrencyConversion> getUserHistory(String userId) {
        return conversionRepository.findConversionsByUserId(userId);
    }

    public Optional<List<CurrencyConversion>> getConversionByAmountRange(Double minAmount, Double maxAmount) {
        return conversionRepository.findConversionByAmountRange(minAmount, maxAmount);
    }

    public List<CurrencyConversion> findByCurrencyCodeAndDate(String currencyCode, String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDate parsedTimestamp = LocalDate.parse(timestamp, formatter);

        Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Currency " + currencyCode + " not found"));

        return conversionRepository.findByCurrencyCodeAndDate(currency, parsedTimestamp);
    }

    // Метод для обновления курсов валют
    public List<Currency> updateCurrencyRates() {
        return currencyService.updateExchangeRates();
    }
}
