package com.elcinic.utility;

import com.elcinic.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessagesTest {

    @Test
    void from_serviceException_usesMessage() {
        assertEquals("Bad login", ErrorMessages.from(new ServiceException("Bad login")));
    }

    @Test
    void from_sqlException_suggestsDatabase() {
        String msg = ErrorMessages.from(new RuntimeException(new SQLException("Unknown column 'account_status'")));
        assertTrue(msg.contains("Database") || msg.contains("MySQL") || msg.contains("Tomcat"));
    }
}
