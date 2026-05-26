package com.elcinic.model;

public enum LabStatus {
    ORDERED, IN_PROGRESS, COMPLETED, CANCELLED;

    public static LabStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lab status required");
        }
        return LabStatus.valueOf(value.trim().toUpperCase());
    }
}
