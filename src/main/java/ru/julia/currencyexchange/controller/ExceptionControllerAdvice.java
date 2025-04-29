package ru.julia.currencyexchange.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.julia.currencyexchange.application.exceptions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                ex.getMessage(),
                "USER_NOT_FOUND"
        ), HttpStatus.NOT_FOUND);
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String error, String message, String errorCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("errorCode", errorCode);
        return body;
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<Object> handleUserCreationException(UserCreationException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST,
                "User Creation Failed",
                ex.getMessage(),
                "USER_CREATION_FAILED"
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<Object> handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.NOT_FOUND,
                "Currency Not Found",
                ex.getMessage(),
                "CURRENCY_NOT_FOUND"
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CurrencyRateFetchException.class)
    public ResponseEntity<Object> handleCurrencyRateFetchException(CurrencyRateFetchException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Failed to Fetch Currency Rates",
                ex.getMessage(),
                "CURRENCY_RATE_FETCH_FAILED"
        ), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(CurrencyRateParsingException.class)
    public ResponseEntity<Object> handleCurrencyRateParsingException(CurrencyRateParsingException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Currency Rate Parsing Error",
                ex.getMessage(),
                "CURRENCY_RATE_PARSING_ERROR"
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CurrencyRateSaveException.class)
    public ResponseEntity<Object> handleCurrencyRateSaveException(CurrencyRateSaveException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Currency Save Error",
                ex.getMessage(),
                "CURRENCY_SAVE_FAILED"
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<Object> handleInvalidDateFormat(InvalidDateFormatException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST,
                "Invalid Date Format",
                ex.getMessage() + ". Expected format: yyyy-MM-dd (e.g. 2024-12-31)",
                "INVALID_DATE_FORMAT"
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Object> handleArithmeticException(ArithmeticException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST,
                "Arithmetic Error in Currency Conversion",
                "An error occurred during currency conversion: " + ex.getMessage(),
                "CURRENCY_ARITHMETIC_ERROR"
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDatabaseError(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.BAD_REQUEST,
                "Database Error",
                ex.getMostSpecificCause().getMessage(),
                "DATABASE_ERROR"
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return new ResponseEntity<>(createErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                "INTERNAL_ERROR"
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
