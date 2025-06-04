package ru.julia.currencyexchange.application.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.julia.currencyexchange.application.dto.CurrencyRate;
import ru.julia.currencyexchange.application.dto.CurrencyRatesList;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateFetchException;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateParsingException;
import ru.julia.currencyexchange.application.exceptions.CurrencyRateSaveException;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {
    private static final String CBR_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public List<Currency> updateExchangeRates() {
        try {
            String xmlResponse = fetchCurrencyRatesXml();
            Map<String, CurrencyRate> rates = parseCurrencyRates(xmlResponse);
            saveCurrencyRates(rates);
            return (List<Currency>) currencyRepository.findAll();
        } catch (Exception e) {
            throw new CurrencyRateSaveException("Не удалось обновить курсы валют: " + e.getMessage(), e);
        }
    }

    private String fetchCurrencyRatesXml() {
        try {
            return restTemplate.getForObject(CBR_URL, String.class);
        } catch (RestClientException e) {
            throw new CurrencyRateFetchException("Ошибка при получении курсов валют от ЦБ РФ: " + e.getMessage());
        }
    }

    private Map<String, CurrencyRate> parseCurrencyRates(String xmlResponse) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            CurrencyRatesList ratesList = xmlMapper.readValue(xmlResponse, CurrencyRatesList.class);

            Map<String, CurrencyRate> rates = new HashMap<>();
            rates.put("RUB", new CurrencyRate("RUB", 1, "Российский рубль", "1.0"));

            for (CurrencyRate rate : ratesList.getValute()) {
                rates.put(rate.getCharCode(), rate);
            }

            return rates;
        } catch (IOException e) {
            throw new CurrencyRateParsingException("Ошибка парсинга XML с курсами валют: " + e.getMessage());
        }
    }

    private void saveCurrencyRates(Map<String, CurrencyRate> rates) {
        for (Map.Entry<String, CurrencyRate> entry : rates.entrySet()) {
            String code = entry.getKey();
            CurrencyRate rate = entry.getValue();
            String currencyName = rate.getName();

            if (rate.getNominal() == 0) {
                throw new ArithmeticException("Номинал валюты " + rate.getCharCode() + " равен 0, деление невозможно.");
            }

            BigDecimal exchangeRate = calculateExchangeRate(rate);

            Currency currency = currencyRepository.findByCode(code)
                    .orElse(new Currency(code, currencyName, exchangeRate));

            currency.setExchangeRate(exchangeRate);
            currency.setName(currencyName);

            try {
                currencyRepository.save(currency);
            } catch (DataIntegrityViolationException e) {
                throw new DataIntegrityViolationException("Ошибка сохранения валюты в БД: " + e.getMessage(), e);
            }
        }
    }

    private BigDecimal calculateExchangeRate(CurrencyRate rate) {
        return BigDecimal.valueOf(rate.getValue())
                .divide(BigDecimal.valueOf(rate.getNominal()), 6, RoundingMode.HALF_UP);
    }
}
