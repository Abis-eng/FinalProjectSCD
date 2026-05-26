package com.elcinic.utility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void requireNonBlank_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.requireNonBlank(null, "Name"));
    }

    @Test
    void validateEmail_acceptsValid() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user@elcinic.com"));
    }

    @Test
    void validateEmail_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateEmail("bad-email"));
    }

    @Test
    void validateUsername_rejectsShort() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateUsername("ab"));
    }

    @Test
    void validatePassword_requiresMinLength() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validatePassword("123"));
    }

    @Test
    void parseDate_parsesIsoDate() {
        LocalDate date = ValidationUtil.parseDate("2026-05-20", "Appointment date");
        assertEquals(LocalDate.of(2026, 5, 20), date);
    }

    @Test
    void parsePositiveId_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveId("0", "Id"));
    }

    @Test
    void validateUsername_acceptsUnderscoreAndDot() {
        assertDoesNotThrow(() -> ValidationUtil.validateUsername("dr.smith_01"));
    }

    @Test
    void validateUsername_rejectsSpecialChars() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.validateUsername("user@name"));
    }

    @Test
    void parseDate_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parseDate("32-13-2026", "Date"));
    }

    @Test
    void parsePositiveId_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveId("-5", "Id"));
    }
}
