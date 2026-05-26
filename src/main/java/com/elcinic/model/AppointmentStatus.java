package com.elcinic.model;

public enum AppointmentStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED;

    public static AppointmentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        return AppointmentStatus.valueOf(value.trim().toUpperCase());
    }
}
