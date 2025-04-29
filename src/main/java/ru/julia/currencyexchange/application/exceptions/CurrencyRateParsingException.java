package ru.julia.currencyexchange.application.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CurrencyRateParsingException extends RuntimeException {

    public CurrencyRateParsingException(String message) {
        super(message);
    }
}
