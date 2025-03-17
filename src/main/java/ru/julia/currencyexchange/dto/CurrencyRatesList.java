package ru.julia.currencyexchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "ValCurs")
public class CurrencyRatesList {
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CurrencyRate> Valute;

    public List<CurrencyRate> getValute() {
        return Valute;
    }
}
