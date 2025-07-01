package ru.julia.currencyexchange.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.julia.currencyexchange.application.dto.common.ApiResponseDto;
import ru.julia.currencyexchange.application.exceptions.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDto<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest().body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(InvalidParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidParameterException(InvalidParameterException ex) {
        return ResponseEntity.badRequest().body(ApiResponseDto.error(ex.getMessage(), 400));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDto<String>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Ошибка валидации");
        
        return ResponseEntity.badRequest().body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDto<String>> handleMissingParams(MissingServletRequestParameterException ex) {
        String message;
        switch (ex.getParameterName()) {
            case "username" -> message = "Username is required";
            case "password" -> message = "Password is required";
            case "preferredCurrency" -> message = "Preferred currency is required";
            default -> message = ex.getParameterName() + " is required";
        }
        
        return ResponseEntity.badRequest().body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<ApiResponseDto<String>> handleUserCreationException(UserCreationException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(ex.getMessage(), 400));
    }

    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(CurrencyRateFetchException.class)
    public ResponseEntity<ApiResponseDto<String>> handleCurrencyRateFetchException(CurrencyRateFetchException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponseDto.error(ex.getMessage(), 503));
    }

    @ExceptionHandler(CurrencyRateParsingException.class)
    public ResponseEntity<ApiResponseDto<String>> handleCurrencyRateParsingException(CurrencyRateParsingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), 500));
    }

    @ExceptionHandler(CurrencyRateSaveException.class)
    public ResponseEntity<ApiResponseDto<String>> handleCurrencyRateSaveException(CurrencyRateSaveException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), 500));
    }

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ApiResponseDto<String>> handleInvalidDateFormat(InvalidDateFormatException ex) {
        String message = ex.getMessage() + ". Expected format: yyyy-MM-dd (e.g. 2024-12-31)";
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<ApiResponseDto<String>> handleArithmeticException(ArithmeticException ex) {
        String message = "An error occurred during currency conversion: " + ex.getMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<String>> handleDatabaseError(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.error(message, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleGenericException(Exception ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(message, 500));
    }
}
