package com.elcinic.model;

public enum ProviderType {
    DOCTOR, NURSE;

    public static ProviderType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Provider type is required");
        }
        return ProviderType.valueOf(value.trim().toUpperCase());
    }
}
