package ru.julia.currencyexchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRate {
    @JacksonXmlProperty(localName = "CharCode")
    private String charCode;

    @JacksonXmlProperty(localName = "Nominal")
    private int nominal;

    @JacksonXmlProperty(localName = "Value")
    private String value; // ЦБ РФ передает значение в виде строки с запятой

    public String getCharCode() {
        return charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public double getValue() {
        return Double.parseDouble(value.replace(",", ".")); // Преобразуем в число
    }
}
