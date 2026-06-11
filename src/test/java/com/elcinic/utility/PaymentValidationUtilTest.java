package com.elcinic.utility;

import com.elcinic.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentValidationUtilTest {

    @Test
    void acceptsValidTestCard() {
        String digits = PaymentValidationUtil.normalizeCardNumber("4111 1111 1111 1111");
        assertEquals("4111111111111111", digits);
        assertEquals("**** **** **** 1111", PaymentValidationUtil.maskCardNumber(digits));
    }

    @Test
    void rejectsInvalidCardNumber() {
        assertThrows(ServiceException.class,
                () -> PaymentValidationUtil.normalizeCardNumber("1234 5678 9012 3456"));
    }

    @Test
    void validatesPakistaniMobile() {
        assertEquals("03001234567", PaymentValidationUtil.normalizeMobileWallet("0300-1234567"));
    }

    @Test
    void rejectsInvalidMobile() {
        assertThrows(ServiceException.class,
                () -> PaymentValidationUtil.normalizeMobileWallet("04001234567"));
    }

    @Test
    void validatesWalletOtp() {
        assertDoesNotThrow(() -> PaymentValidationUtil.validateWalletOtp("123456"));
        assertThrows(ServiceException.class, () -> PaymentValidationUtil.validateWalletOtp("12345"));
    }

    @Test
    void patientMethodsExcludeCash() {
        assertDoesNotThrow(() -> PaymentValidationUtil.requirePatientOnlineMethod("CARD"));
        assertThrows(ServiceException.class, () -> PaymentValidationUtil.requirePatientOnlineMethod("CASH"));
    }
}
