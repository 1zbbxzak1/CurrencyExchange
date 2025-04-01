package ru.julia.currencyexchange.entity.enums;

public enum RoleEnum {
    ADMIN("Admin"),
    USER("User");

    private final String roleName;

    RoleEnum(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
