package com.elcinic.model;

public enum Role {
    ADMIN, DOCTOR, NURSE, PATIENT;

    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
