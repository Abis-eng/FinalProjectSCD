package com.elcinic.model;

public enum PaymentStatus {
    PENDING, PAID, WAIVED, REFUNDED;

    public static PaymentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment status required");
        }
        return PaymentStatus.valueOf(value.trim().toUpperCase());
    }
}
