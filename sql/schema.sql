-- E-Clinic Database Schema (MySQL / XAMPP)
-- Run: mysql -u root < sql/schema.sql   OR import via phpMyAdmin

CREATE DATABASE IF NOT EXISTS elcinic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elcinic;

DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS nurses;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(128) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    role            ENUM('ADMIN','DOCTOR','NURSE','PATIENT') NOT NULL,
    active          TINYINT(1) DEFAULT 1,
    account_status  ENUM('ACTIVE','PENDING','REJECTED') DEFAULT 'ACTIVE',
    verified_at     TIMESTAMP NULL,
    verified_by     INT NULL,
    rejection_reason VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctors (
    user_id             INT PRIMARY KEY,
    specialization      VARCHAR(100) NOT NULL,
    license_number      VARCHAR(50)  NOT NULL UNIQUE,
    consultation_fee    DECIMAL(10,2) DEFAULT 3000.00,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE nurses (
    user_id         INT PRIMARY KEY,
    department      VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE patients (
    user_id             INT PRIMARY KEY,
    date_of_birth       DATE,
    blood_type          VARCHAR(5),
    assigned_doctor_id  INT NULL,
    requested_doctor_id INT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_doctor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (requested_doctor_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE appointments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    provider_id     INT NOT NULL,
    provider_type   ENUM('DOCTOR','NURSE') NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot       VARCHAR(20) NOT NULL,
    status          ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    reason          VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE chat_messages (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    sender_id        INT NOT NULL,
    receiver_id      INT NOT NULL,
    appointment_id   INT NULL,
    content          TEXT NOT NULL,
    is_read          TINYINT(1) DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

CREATE TABLE medical_records (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    doctor_id       INT NOT NULL,
    visit_date      DATE NOT NULL,
    diagnosis       VARCHAR(255) NOT NULL,
    prescription    TEXT,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Default admin only (password: admin123). Register patients, doctors, and nurses via the app.
INSERT INTO users (username, password_hash, full_name, email, phone, role, account_status) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
 'System Administrator', 'admin@elcinic.com', '0000000000', 'ADMIN', 'ACTIVE');
