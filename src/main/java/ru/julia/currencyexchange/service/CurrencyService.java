package ru.julia.currencyexchange.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.julia.currencyexchange.dto.CurrencyRate;
import ru.julia.currencyexchange.dto.CurrencyRatesList;
import ru.julia.currencyexchange.entity.Currency;
import ru.julia.currencyexchange.repository.CurrencyRepository;

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
            // Получаем XML-ответ
            String xmlResponse = restTemplate.getForObject(CBR_URL, String.class);
            XmlMapper xmlMapper = new XmlMapper();
            CurrencyRatesList ratesList = xmlMapper.readValue(xmlResponse, CurrencyRatesList.class);

            // Создаем мапу с курсами
            Map<String, CurrencyRate> rates = new HashMap<>();
            rates.put("RUB", new CurrencyRate("RUB", 1, "Российский рубль", "1.0"));

            for (CurrencyRate rate : ratesList.getValute()) {
                rates.put(rate.getCharCode(), rate);
            }

            for (Map.Entry<String, CurrencyRate> entry : rates.entrySet()) {
                String code = entry.getKey();
                CurrencyRate rate = entry.getValue();
                BigDecimal exchangeRate = BigDecimal.valueOf(rate.getValue()).divide(BigDecimal.valueOf(rate.getNominal()), 6, RoundingMode.HALF_UP);
                String currencyName = rate.getName();

                Currency currency = currencyRepository.findByCode(code)
                        .orElse(new Currency(code, currencyName, exchangeRate));

                currency.setExchangeRate(exchangeRate);
                currency.setName(currencyName);
                currencyRepository.save(currency);
            }

            return (List<Currency>) currencyRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения и сохранения курсов валют: " + e.getMessage(), e);
        }
    }
}
