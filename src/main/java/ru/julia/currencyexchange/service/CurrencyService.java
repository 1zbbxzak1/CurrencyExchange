package ru.julia.currencyexchange.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.julia.currencyexchange.dto.CurrencyRate;
import ru.julia.currencyexchange.dto.CurrencyRatesList;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {
    private static final String CBR_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private final RestTemplate restTemplate;

    public CurrencyService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Double> getExchangeRates() {
        try {
            // Получаем XML-ответ
            String xmlResponse = restTemplate.getForObject(CBR_URL, String.class);
            XmlMapper xmlMapper = new XmlMapper();
            CurrencyRatesList ratesList = xmlMapper.readValue(xmlResponse, CurrencyRatesList.class);

            // Создаем мапу с курсами
            Map<String, Double> rates = new HashMap<>();
            rates.put("RUB", 1.0); // Рубль добавляем вручную

            for (CurrencyRate rate : ratesList.getValute()) {
                rates.put(rate.getCharCode(), rate.getValue() / rate.getNominal());
            }

            return rates;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения курсов валют: " + e.getMessage(), e);
        }
    }
}
