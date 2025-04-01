package ru.julia.currencyexchange.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CurrencyRateFetchException extends RuntimeException {

    public CurrencyRateFetchException(String message) {
        super(message);
    }
}