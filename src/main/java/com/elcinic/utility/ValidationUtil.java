package com.elcinic.utility;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._]{3,50}$");

    private ValidationUtil() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    public static void validateEmail(String email) {
        requireNonBlank(email, "Email");
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validateUsername(String username) {
        requireNonBlank(username, "Username");
        if (!USERNAME.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException("Username must be 3-50 alphanumeric characters");
        }
    }

    public static void validatePassword(String password) {
        requireNonBlank(password, "Password");
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    public static LocalDate parseDate(String date, String fieldName) {
        requireNonBlank(date, fieldName);
        try {
            return LocalDate.parse(date.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid " + fieldName);
        }
    }

    public static int parsePositiveId(String id, String fieldName) {
        requireNonBlank(id, fieldName);
        try {
            int value = Integer.parseInt(id.trim());
            if (value <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName);
        }
    }
}
