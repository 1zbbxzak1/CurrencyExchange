package ru.julia.currencyexchange.domain.enums;

public enum StatusEnum {
    CREATED("Создан"),
    COMPLETED("Завершен"),
    FAILED("Ошибка");

    private final String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
