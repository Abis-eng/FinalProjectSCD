package com.elcinic.model;

public enum AppointmentType {
    CONSULTATION, FOLLOW_UP, EMERGENCY, VACCINATION;

    public static AppointmentType fromString(String value) {
        if (value == null || value.isBlank()) {
            return CONSULTATION;
        }
        return AppointmentType.valueOf(value.trim().toUpperCase());
    }

    /** Multiplier applied to the doctor's consultation fee (or nurse base fee). */
    public double feeMultiplier() {
        return switch (this) {
            case EMERGENCY -> 2.5;
            case VACCINATION -> 1.35;
            case FOLLOW_UP -> 0.65;
            default -> 1.0;
        };
    }

    public String labelPkrHint() {
        return switch (this) {
            case EMERGENCY -> "Emergency (×2.5)";
            case VACCINATION -> "Vaccination (×1.35)";
            case FOLLOW_UP -> "Follow-up (×0.65)";
            default -> "Consultation (base fee)";
        };
    }
}
