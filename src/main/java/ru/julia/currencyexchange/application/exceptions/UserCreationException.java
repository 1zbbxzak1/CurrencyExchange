package ru.julia.currencyexchange.application.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserCreationException extends RuntimeException {

    public UserCreationException(String message) {
        super(message);
    }
}
