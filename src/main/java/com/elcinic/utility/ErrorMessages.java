package com.elcinic.utility;

import com.elcinic.exception.ServiceException;

import java.sql.SQLException;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String from(Throwable e) {
        if (e == null) {
            return "An unexpected error occurred";
        }
        if (e instanceof ServiceException) {
            return e.getMessage();
        }
        Throwable root = unwrap(e);
        if (root instanceof SQLException) {
            String sql = root.getMessage();
            if (sql != null && sql.contains("account_status")) {
                return "Database needs an update. Restart Tomcat with MySQL running (XAMPP).";
            }
            if (sql != null && (sql.contains("Communications link failure")
                    || sql.contains("Connection refused")
                    || sql.contains("connect")
                    || sql.contains("Unknown database"))) {
                return "Cannot connect to MySQL. Open XAMPP, start MySQL, then restart Tomcat.";
            }
            return "Database error. Ensure MySQL is running and restart Tomcat.";
        }
        if (root.getMessage() != null && !root.getMessage().isBlank()) {
            return root.getMessage();
        }
        return "An unexpected error occurred";
    }

    private static Throwable unwrap(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
