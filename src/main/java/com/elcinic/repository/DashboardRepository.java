package com.elcinic.repository;

import com.elcinic.model.ChartSeries;
import com.elcinic.model.DashboardStats;
import com.elcinic.utility.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardRepository {

    public DashboardStats loadAdminStats() {
        DashboardStats s = new DashboardStats();
        s.setTotalPatients(count("SELECT COUNT(*) FROM users WHERE role='PATIENT' AND active=1"));
        s.setTotalDoctors(count("SELECT COUNT(*) FROM users WHERE role='DOCTOR' AND active=1"));
        s.setTodayAppointments(count("SELECT COUNT(*) FROM appointments WHERE appointment_date=CURDATE()"));
        s.setPendingAppointments(count("SELECT COUNT(*) FROM appointments WHERE status IN ('PENDING','CONFIRMED')"));
        s.setCompletedToday(count("SELECT COUNT(*) FROM appointments WHERE status='COMPLETED' AND appointment_date=CURDATE()"));
        s.setUnpaidInvoices(count("SELECT COUNT(*) FROM invoices WHERE status='PENDING'"));
        s.setRevenueToday(sumPaidToday());
        s.setPendingLabs(count("SELECT COUNT(*) FROM lab_tests WHERE status IN ('ORDERED','IN_PROGRESS')"));
        return s;
    }

    public DashboardStats loadForUser(int userId, String role) {
        DashboardStats s = new DashboardStats();
        if ("DOCTOR".equals(role)) {
            s.setTodayAppointments(count(
                    "SELECT COUNT(*) FROM appointments WHERE provider_id=" + userId + " AND appointment_date=CURDATE()"));
            s.setPendingAppointments(count(
                    "SELECT COUNT(*) FROM appointments WHERE provider_id=" + userId + " AND status IN ('PENDING','CONFIRMED')"));
        } else if ("PATIENT".equals(role)) {
            s.setTodayAppointments(count(
                    "SELECT COUNT(*) FROM appointments WHERE patient_id=" + userId + " AND appointment_date=CURDATE()"));
            s.setPendingAppointments(count(
                    "SELECT COUNT(*) FROM appointments WHERE patient_id=" + userId + " AND status IN ('PENDING','CONFIRMED')"));
            s.setUnpaidInvoices(count(
                    "SELECT COUNT(*) FROM invoices WHERE patient_id=" + userId + " AND status='PENDING'"));
        } else if ("NURSE".equals(role)) {
            s.setTodayAppointments(count(
                    "SELECT COUNT(*) FROM appointments WHERE provider_id=" + userId + " AND appointment_date=CURDATE()"));
        }
        return s;
    }

    private double sumPaidToday() {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(SUM(amount),0) FROM invoices WHERE status='PAID' AND DATE(paid_at)=CURDATE()")) {
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            return 0;
        }
    }

    public ChartSeries weeklyAppointments() {
        ChartSeries series = new ChartSeries();
        String sql = """
                SELECT DATE_FORMAT(appointment_date, '%a %d') AS lbl, COUNT(*) AS cnt
                FROM appointments
                WHERE appointment_date >= CURDATE() - INTERVAL 6 DAY
                GROUP BY appointment_date
                ORDER BY appointment_date
                """;
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                series.add(rs.getString("lbl"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            // empty chart
        }
        return series;
    }

    public ChartSeries appointmentsByStatus() {
        ChartSeries series = new ChartSeries();
        String sql = """
                SELECT status, COUNT(*) AS cnt FROM appointments
                GROUP BY status ORDER BY cnt DESC
                """;
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                series.add(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            // empty
        }
        return series;
    }

    public ChartSeries revenueLast7Days() {
        ChartSeries series = new ChartSeries();
        String sql = """
                SELECT DATE_FORMAT(DATE(paid_at), '%a %d') AS lbl, COALESCE(SUM(amount),0) AS total
                FROM invoices
                WHERE status = 'PAID' AND paid_at >= CURDATE() - INTERVAL 6 DAY
                GROUP BY DATE(paid_at)
                ORDER BY DATE(paid_at)
                """;
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                series.add(rs.getString("lbl"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            // empty
        }
        return series;
    }

    private int count(String sql) {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
    }
}
