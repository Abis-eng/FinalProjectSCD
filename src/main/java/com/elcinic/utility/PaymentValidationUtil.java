package com.elcinic.utility;

import com.elcinic.exception.ServiceException;

import java.time.YearMonth;
import java.util.Set;

public final class PaymentValidationUtil {

    private static final Set<String> PATIENT_ONLINE_METHODS = Set.of("CARD", "ONLINE", "BANK_TRANSFER");

    private PaymentValidationUtil() {
    }

    public static void requirePatientOnlineMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new ServiceException("Please select a payment method");
        }
        String normalized = method.trim().toUpperCase();
        if (!PATIENT_ONLINE_METHODS.contains(normalized)) {
            throw new ServiceException("Patients can pay online by card, mobile wallet, or bank transfer only");
        }
    }

    public static String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            throw new ServiceException("Card number is required");
        }
        String digits = cardNumber.replaceAll("\\s+", "");
        if (!digits.matches("\\d{16}")) {
            throw new ServiceException("Enter a valid 16-digit card number");
        }
        if (!passesLuhn(digits)) {
            throw new ServiceException("Card number is invalid");
        }
        return digits;
    }

    public static String normalizeExpiry(String expiry) {
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}")) {
            throw new ServiceException("Expiry must be MM/YY");
        }
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3, 5));
        if (month < 1 || month > 12) {
            throw new ServiceException("Invalid expiry month");
        }
        YearMonth exp = YearMonth.of(year, month);
        if (exp.isBefore(YearMonth.now())) {
            throw new ServiceException("Card has expired");
        }
        return expiry;
    }

    public static void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new ServiceException("Enter a valid 3 or 4 digit CVV");
        }
    }

    public static void validateCardHolderName(String name) {
        if (name == null || name.trim().length() < 3) {
            throw new ServiceException("Enter the name on card");
        }
    }

    public static String normalizeMobileWallet(String mobile) {
        if (mobile == null) {
            throw new ServiceException("Mobile number is required");
        }
        String digits = mobile.replaceAll("\\D", "");
        if (!digits.matches("03\\d{9}")) {
            throw new ServiceException("Enter a valid Pakistani mobile number (03XXXXXXXXX)");
        }
        return digits;
    }

    public static void validateWalletOtp(String otp) {
        if (otp == null || !otp.matches("\\d{6}")) {
            throw new ServiceException("Enter the 6-digit OTP sent to your mobile");
        }
    }

    public static void validateBankTransfer(String bankName, String transactionRef) {
        if (bankName == null || bankName.trim().length() < 2) {
            throw new ServiceException("Bank name is required");
        }
        if (transactionRef == null || transactionRef.trim().length() < 6) {
            throw new ServiceException("Enter your bank transaction reference");
        }
    }

    public static String maskCardNumber(String digits) {
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    private static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
