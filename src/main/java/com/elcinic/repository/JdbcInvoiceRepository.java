package com.elcinic.repository;

import com.elcinic.model.Invoice;
import com.elcinic.model.PaymentStatus;
import com.elcinic.utility.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcInvoiceRepository implements InvoiceRepository {

    private static final String BASE = """
            SELECT i.*, pu.full_name AS patient_name, pr.full_name AS provider_name,
                   a.appointment_date
            FROM invoices i
            JOIN users pu ON i.patient_id = pu.id
            JOIN appointments a ON i.appointment_id = a.id
            JOIN users pr ON a.provider_id = pr.id
            """;

    @Override
    public Optional<Invoice> findByAppointmentId(int appointmentId) {
        return queryOne(BASE + " WHERE i.appointment_id = ?", appointmentId);
    }

    @Override
    public Optional<Invoice> findById(int id) {
        return queryOne(BASE + " WHERE i.id = ?", id);
    }

    @Override
    public List<Invoice> findByPatient(int patientId) {
        return queryList(BASE + " WHERE i.patient_id = ? ORDER BY i.created_at DESC", patientId);
    }

    @Override
    public List<Invoice> findAll(String keyword, PaymentStatus status) {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND pu.full_name LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null) {
            sql.append(" AND i.status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY i.created_at DESC");
        return queryList(sql.toString(), params.toArray());
    }

    @Override
    public int create(Invoice invoice) {
        String sql = "INSERT INTO invoices (appointment_id, patient_id, amount, status) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getAppointmentId());
            ps.setInt(2, invoice.getPatientId());
            ps.setBigDecimal(3, invoice.getAmount());
            ps.setString(4, invoice.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No invoice id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePayment(int id, PaymentStatus status, String paymentMethod) {
        updatePayment(id, status, paymentMethod, null, null);
    }

    @Override
    public void updatePayment(int id, PaymentStatus status, String paymentMethod,
                              String paymentReference, String cardLast4) {
        String sql = """
                UPDATE invoices SET status=?, payment_method=?, payment_reference=?, card_last4=?, paid_at=NOW()
                WHERE id=?""";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, paymentMethod);
            ps.setString(3, paymentReference);
            ps.setString(4, cardLast4);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double sumPaidToday() {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM invoices WHERE status='PAID' AND DATE(paid_at)=CURDATE()";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Invoice> queryOne(String sql, Object... params) {
        List<Invoice> list = queryList(sql, params);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<Invoice> queryList(String sql, Object... params) {
        List<Invoice> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setId(rs.getInt("id"));
        i.setAppointmentId(rs.getInt("appointment_id"));
        i.setPatientId(rs.getInt("patient_id"));
        i.setPatientName(rs.getString("patient_name"));
        i.setAmount(rs.getBigDecimal("amount"));
        i.setStatus(PaymentStatus.fromString(rs.getString("status")));
        i.setPaymentMethod(rs.getString("payment_method"));
        i.setPaymentReference(rs.getString("payment_reference"));
        i.setCardLast4(rs.getString("card_last4"));
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) {
            i.setPaidAt(paid.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            i.setCreatedAt(created.toLocalDateTime());
        }
        Date ad = rs.getDate("appointment_date");
        if (ad != null) {
            i.setAppointmentDate(ad.toString());
        }
        i.setProviderName(rs.getString("provider_name"));
        return i;
    }
}
