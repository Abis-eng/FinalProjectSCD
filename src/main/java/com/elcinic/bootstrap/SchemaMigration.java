package com.elcinic.bootstrap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Idempotent schema updates for databases created before account verification was added.
 */
public final class SchemaMigration {

    private SchemaMigration() {
    }

    public static void ensureLatest(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            if (!columnExists(conn, "users", "account_status")) {
                st.executeUpdate(
                        "ALTER TABLE users ADD COLUMN account_status ENUM('ACTIVE','PENDING','REJECTED') DEFAULT 'ACTIVE'");
            }
            if (!columnExists(conn, "users", "verified_at")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN verified_at TIMESTAMP NULL");
            }
            if (!columnExists(conn, "users", "verified_by")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN verified_by INT NULL");
            }
            if (!columnExists(conn, "users", "rejection_reason")) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN rejection_reason VARCHAR(255)");
            }
            st.executeUpdate("UPDATE users SET account_status = 'ACTIVE' WHERE account_status IS NULL");
            if (!columnExists(conn, "doctors", "consultation_fee")) {
                st.executeUpdate("ALTER TABLE doctors ADD COLUMN consultation_fee DECIMAL(10,2) DEFAULT 3000.00");
            }
            st.executeUpdate("UPDATE doctors SET consultation_fee = 3000.00 WHERE consultation_fee IS NULL");
            if (!columnExists(conn, "patients", "requested_doctor_id")) {
                st.executeUpdate("ALTER TABLE patients ADD COLUMN requested_doctor_id INT NULL");
            }
            if (!tableExists(conn, "chat_messages")) {
                st.executeUpdate("""
                        CREATE TABLE chat_messages (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            sender_id INT NOT NULL,
                            receiver_id INT NOT NULL,
                            appointment_id INT NULL,
                            content TEXT NOT NULL,
                            is_read TINYINT(1) DEFAULT 0,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
                        )""");
            }
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, table, null)) {
            return rs.next();
        }
    }
}
