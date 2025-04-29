package ru.julia.currencyexchange.application.service;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.exceptions.CurrencyNotFoundException;
import ru.julia.currencyexchange.application.exceptions.InvalidDateFormatException;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ConversionRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyExchangeService {
    private final CurrencyService currencyService;
    private final CurrencyRepository currencyRepository;
    private final ConversionRepository conversionRepository;
    private final UserRepository userRepository;

    public CurrencyExchangeService(CurrencyService currencyService,
                                   CurrencyRepository currencyRepository,
                                   ConversionRepository conversionRepository,
                                   UserRepository userRepository) {
        this.currencyService = currencyService;
        this.currencyRepository = currencyRepository;
        this.conversionRepository = conversionRepository;
        this.userRepository = userRepository;
    }

    public CurrencyConversion convert(String userId, String from, String to, BigDecimal amount) {
        currencyService.updateExchangeRates();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));


        Currency fromCurrency = currencyRepository.findByCode(from)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency " + from + " not found"));

        Currency toCurrency = currencyRepository.findByCode(to)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency " + to + " not found"));

        if (toCurrency.getExchangeRate().compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Exchange rate for currency " + to + " is zero, cannot divide");
        }

        BigDecimal rate = fromCurrency.getExchangeRate()
                .divide(toCurrency.getExchangeRate(), 6, RoundingMode.HALF_UP);


        BigDecimal convertedAmount = amount.multiply(rate);

        CurrencyConversion conversion = new CurrencyConversion(user,
                fromCurrency,
                toCurrency,
                amount,
                convertedAmount,
                rate);
        return conversionRepository.save(conversion);
    }

    public List<CurrencyConversion> getUserHistory(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        return conversionRepository.findConversionByUserId(user.getId());
    }

    public Optional<List<CurrencyConversion>> getConversionByAmountRange(Double minAmount, Double maxAmount) {
        return conversionRepository.findConversionByAmountRange(minAmount, maxAmount);
    }

    public List<CurrencyConversion> findByCurrencyCodeAndDate(String currencyCode, String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        try {
            LocalDate parsedTimestamp = LocalDate.parse(timestamp, formatter);

            Currency currency = currencyRepository.findByCode(currencyCode)
                    .orElseThrow(() -> new CurrencyNotFoundException("Currency " + currencyCode + " not found"));

            return conversionRepository.findByCurrencyCodeAndDate(currency, parsedTimestamp);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Invalid date format: " + timestamp);
        }
    }

    // Метод для обновления курсов валют
    public List<Currency> updateCurrencyRates() {
        return currencyService.updateExchangeRates();
    }
}
