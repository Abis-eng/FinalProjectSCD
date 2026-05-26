package com.elcinic.bootstrap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Creates {@code elcinic} database and tables on deploy if missing (XAMPP MySQL).
 * Start MySQL in XAMPP before running Smart Tomcat.
 */
@WebListener
public class ElcinicDbListener implements ServletContextListener {

    private static final String DB_NAME = "elcinic";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String user = envOrInit(ctx, "DB_USER", "mysql.user");
        if (user == null) {
            return;
        }
        String password = envOrInit(ctx, "DB_PASSWORD", "mysql.password");
        if (password == null) {
            password = "";
        }
        String bootstrapUrl = envOrInit(ctx, "MYSQL_BOOTSTRAP_URL", "mysql.bootstrapUrl");
        if (bootstrapUrl == null || bootstrapUrl.isBlank()) {
            bootstrapUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(bootstrapUrl, user, password);
                 Statement st = c.createStatement()) {
                st.executeUpdate(
                        "CREATE DATABASE IF NOT EXISTS " + DB_NAME
                                + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            String appUrl = envOrInit(ctx, "DB_URL", "mysql.jdbcUrl");
            if (appUrl == null || appUrl.isBlank()) {
                appUrl = "jdbc:mysql://localhost:3306/" + DB_NAME
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            }

            try (Connection c = DriverManager.getConnection(appUrl, user, password);
                 Statement st = c.createStatement()) {
                createTables(st);
                SchemaMigration.ensureLatest(c);
                migrateSchema(st);
                seedIfEmpty(st);
            }
            ctx.log("ElcinicDbListener: database ready");
        } catch (Exception e) {
            ctx.log("ElcinicDbListener: MySQL init failed — is XAMPP MySQL running?", e);
        }
    }

    private static String envOrInit(ServletContext ctx, String envKey, String initKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return ctx.getInitParameter(initKey);
    }

    private void createTables(Statement st) throws Exception {
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(128) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    phone VARCHAR(20),
                    role ENUM('ADMIN','DOCTOR','NURSE','PATIENT') NOT NULL,
                    active TINYINT(1) DEFAULT 1,
                    account_status ENUM('ACTIVE','PENDING','REJECTED') DEFAULT 'ACTIVE',
                    verified_at TIMESTAMP NULL,
                    verified_by INT NULL,
                    rejection_reason VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS doctors (
                    user_id INT PRIMARY KEY,
                    specialization VARCHAR(100) NOT NULL,
                    license_number VARCHAR(50) NOT NULL UNIQUE,
                    consultation_fee DECIMAL(10,2) DEFAULT 3000.00,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS nurses (
                    user_id INT PRIMARY KEY,
                    department VARCHAR(100) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patients (
                    user_id INT PRIMARY KEY,
                    date_of_birth DATE,
                    blood_type VARCHAR(5),
                    assigned_doctor_id INT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (assigned_doctor_id) REFERENCES users(id) ON DELETE SET NULL
                )""");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT NOT NULL,
                    provider_id INT NOT NULL,
                    provider_type ENUM('DOCTOR','NURSE') NOT NULL,
                    appointment_date DATE NOT NULL,
                    time_slot VARCHAR(20) NOT NULL,
                    status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
                    appointment_type ENUM('CONSULTATION','FOLLOW_UP','EMERGENCY','VACCINATION') DEFAULT 'CONSULTATION',
                    priority ENUM('NORMAL','URGENT') DEFAULT 'NORMAL',
                    symptoms TEXT,
                    room_number VARCHAR(20),
                    fee_amount DECIMAL(10,2) DEFAULT 60.00,
                    reason VARCHAR(255),
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS medical_records (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT NOT NULL,
                    doctor_id INT NOT NULL,
                    visit_date DATE NOT NULL,
                    diagnosis VARCHAR(255) NOT NULL,
                    prescription TEXT,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
    }

    private void migrateSchema(Statement st) {
        safeAlter(st, "ALTER TABLE users ADD COLUMN account_status ENUM('ACTIVE','PENDING','REJECTED') DEFAULT 'ACTIVE'");
        safeAlter(st, "ALTER TABLE users ADD COLUMN verified_at TIMESTAMP NULL");
        safeAlter(st, "ALTER TABLE users ADD COLUMN verified_by INT NULL");
        safeAlter(st, "ALTER TABLE users ADD COLUMN rejection_reason VARCHAR(255)");
        safeAlter(st, "UPDATE users SET account_status = 'ACTIVE' WHERE account_status IS NULL");
        safeAlter(st, "ALTER TABLE appointments ADD COLUMN appointment_type ENUM('CONSULTATION','FOLLOW_UP','EMERGENCY','VACCINATION') DEFAULT 'CONSULTATION'");
        safeAlter(st, "ALTER TABLE appointments ADD COLUMN priority ENUM('NORMAL','URGENT') DEFAULT 'NORMAL'");
        safeAlter(st, "ALTER TABLE appointments ADD COLUMN symptoms TEXT");
        safeAlter(st, "ALTER TABLE appointments ADD COLUMN room_number VARCHAR(20)");
        safeAlter(st, "ALTER TABLE appointments ADD COLUMN fee_amount DECIMAL(10,2) DEFAULT 60.00");
        try {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        appointment_id INT NOT NULL UNIQUE,
                        patient_id INT NOT NULL,
                        amount DECIMAL(10,2) NOT NULL,
                        status ENUM('PENDING','PAID','WAIVED','REFUNDED') DEFAULT 'PENDING',
                        payment_method VARCHAR(50),
                        paid_at TIMESTAMP NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
                        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        title VARCHAR(150) NOT NULL,
                        message TEXT NOT NULL,
                        is_read TINYINT(1) DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS prescription_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        medical_record_id INT NOT NULL,
                        medication_name VARCHAR(150) NOT NULL,
                        dosage VARCHAR(80),
                        frequency VARCHAR(80),
                        duration_days INT DEFAULT 7,
                        instructions TEXT,
                        FOREIGN KEY (medical_record_id) REFERENCES medical_records(id) ON DELETE CASCADE
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS vital_signs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        appointment_id INT NOT NULL UNIQUE,
                        blood_pressure VARCHAR(20),
                        pulse INT,
                        temperature DECIMAL(4,1),
                        weight_kg DECIMAL(5,2),
                        height_cm DECIMAL(5,2),
                        recorded_by INT,
                        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
                        FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS lab_tests (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT NOT NULL,
                        doctor_id INT NOT NULL,
                        test_name VARCHAR(150) NOT NULL,
                        result_value VARCHAR(100),
                        result_unit VARCHAR(30),
                        status ENUM('ORDERED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'ORDERED',
                        ordered_date DATE NOT NULL,
                        completed_date DATE,
                        notes TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
                    )""");
        } catch (Exception e) {
            // logged at caller
        }
    }

    private void safeAlter(Statement st, String sql) {
        try {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
            // column may already exist
        }
    }

    private void seedIfEmpty(Statement st) throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }
        st.executeUpdate("""
                INSERT INTO users (username, password_hash, full_name, email, phone, role, account_status) VALUES
                ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
                 'System Administrator', 'admin@elcinic.com', '0000000000', 'ADMIN', 'ACTIVE')
                """);
    }
}
