package ru.julia.currencyexchange.application.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Общий ответ API")
public class ApiResponseDto<T> {
    @Schema(description = "Статус операции", example = "true")
    private boolean success;
    
    @Schema(description = "Сообщение", example = "Операция выполнена успешно")
    private String message;
    
    @Schema(description = "Данные ответа")
    private T data;
    
    @Schema(description = "Код ошибки", example = "400")
    private Integer errorCode;

    public ApiResponseDto() {
    }

    public ApiResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = null;
    }

    public ApiResponseDto(boolean success, String message, T data, Integer errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data, null);
    }

    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "Операция выполнена успешно", data, null);
    }

    public static <T> ApiResponseDto<T> error(String message, Integer errorCode) {
        return new ApiResponseDto<>(false, message, null, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
} 