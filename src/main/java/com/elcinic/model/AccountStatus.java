package com.elcinic.model;

public enum AccountStatus {
    ACTIVE,
    PENDING,
    REJECTED;

    public static AccountStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account status is required");
        }
        return AccountStatus.valueOf(value.trim().toUpperCase());
    }
}
