package com.elcinic.model;

public enum Priority {
    NORMAL, URGENT;

    public static Priority fromString(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }
        return Priority.valueOf(value.trim().toUpperCase());
    }
}
