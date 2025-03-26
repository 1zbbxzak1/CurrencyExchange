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

import java.time.LocalDateTime;
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
    public List<Currency> getExchangeRates() {
        try {
            // Получаем XML-ответ
            String xmlResponse = restTemplate.getForObject(CBR_URL, String.class);
            XmlMapper xmlMapper = new XmlMapper();
            CurrencyRatesList ratesList = xmlMapper.readValue(xmlResponse, CurrencyRatesList.class);

            // Создаем мапу с курсами
            Map<String, CurrencyRate> rates = new HashMap<>();
            rates.put("RUB", new CurrencyRate("RUB", 1, "Российский рубль", "1.0")); // Рубль добавляем вручную

            for (CurrencyRate rate : ratesList.getValute()) {
                rates.put(rate.getCharCode(), rate);
            }

            Map<String, Currency> existingCurrencies = new HashMap<>();
            currencyRepository.findAll().forEach(currency ->
                    existingCurrencies.put(currency.getCode(), currency)
            );

            for (Map.Entry<String, CurrencyRate> entry : rates.entrySet()) {
                String code = entry.getKey();
                CurrencyRate rate = entry.getValue();
                double exchangeRate = rate.getValue() / rate.getNominal();
                String currencyName = rate.getName();

                Currency currency = existingCurrencies.get(code);

                if (currency != null) {
                    // Проверяем, изменился ли курс или имя валюты
                    if (currency.getExchangeRate() != exchangeRate || !currency.getName().equals(currencyName)) {
                        currency.setExchangeRate(exchangeRate);
                        currency.setName(currencyName); // Обновляем имя валюты
                        currency.setDate(LocalDateTime.now());
                        currencyRepository.save(currency);
                    }
                } else {
                    // Создаем новую запись, если её нет в БД
                    currency = new Currency(code, currencyName, exchangeRate, LocalDateTime.now());
                    currencyRepository.save(currency);
                }
            }

            // Возвращаем все валюты из базы данных, приведенные к List
            return (List<Currency>) currencyRepository.findAll();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения и сохранения курсов валют: " + e.getMessage(), e);
        }
    }
}
