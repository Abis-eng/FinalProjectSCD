package com.elcinic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumParsingTest {

    @Test
    void role_fromString_isCaseInsensitive() {
        assertEquals(Role.DOCTOR, Role.fromString("doctor"));
    }

    @Test
    void providerType_fromString() {
        assertEquals(ProviderType.NURSE, ProviderType.fromString("NURSE"));
    }

    @Test
    void appointmentStatus_fromString() {
        assertEquals(AppointmentStatus.PENDING, AppointmentStatus.fromString("pending"));
    }

    @Test
    void role_fromString_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("  "));
    }

    @Test
    void accountStatus_fromString() {
        assertEquals(AccountStatus.PENDING, AccountStatus.fromString("pending"));
    }

    @Test
    void appointmentType_feeMultipliers() {
        assertEquals(1.0, AppointmentType.CONSULTATION.feeMultiplier());
        assertEquals(2.5, AppointmentType.EMERGENCY.feeMultiplier());
        assertEquals(0.65, AppointmentType.FOLLOW_UP.feeMultiplier());
        assertEquals(1.35, AppointmentType.VACCINATION.feeMultiplier());
    }
}
