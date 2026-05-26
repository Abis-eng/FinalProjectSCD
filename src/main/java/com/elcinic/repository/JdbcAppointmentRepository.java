package com.elcinic.repository;

import com.elcinic.model.*;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAppointmentRepository implements AppointmentRepository {

    private static final String BASE_SELECT = """
            SELECT a.*,
                   pu.full_name AS patient_name,
                   pr.full_name AS provider_name
            FROM appointments a
            JOIN users pu ON a.patient_id = pu.id
            JOIN users pr ON a.provider_id = pr.id
            """;

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = BASE_SELECT + " WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find appointment", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll(String keyword, AppointmentStatus status, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status, from, to);
        sql.append(" ORDER BY a.appointment_date DESC, a.time_slot");

        return queryList(sql.toString(), params);
    }

    @Override
    public List<Appointment> findByPatient(int patientId) {
        String sql = BASE_SELECT + " WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";
        List<Object> params = List.of(patientId);
        return queryList(sql, params);
    }

    @Override
    public List<Appointment> findByProvider(int providerId) {
        String sql = BASE_SELECT + " WHERE a.provider_id = ? ORDER BY a.appointment_date DESC";
        return queryList(sql, List.of(providerId));
    }

    @Override
    public int create(Appointment appointment) {
        String sql = """
                INSERT INTO appointments
                (patient_id, provider_id, provider_type, appointment_date, time_slot, status,
                 appointment_type, priority, symptoms, room_number, fee_amount, reason, notes)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getProviderId());
            ps.setString(3, appointment.getProviderType().name());
            ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            ps.setString(5, appointment.getTimeSlot());
            ps.setString(6, appointment.getStatus().name());
            ps.setString(7, appointment.getAppointmentType().name());
            ps.setString(8, appointment.getPriority().name());
            ps.setString(9, appointment.getSymptoms());
            ps.setString(10, appointment.getRoomNumber());
            ps.setBigDecimal(11, appointment.getFeeAmount());
            ps.setString(12, appointment.getReason());
            ps.setString(13, appointment.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No appointment id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create appointment", e);
        }
    }

    @Override
    public void update(Appointment appointment) {
        String sql = """
                UPDATE appointments SET provider_id=?, provider_type=?, appointment_date=?,
                time_slot=?, status=?, appointment_type=?, priority=?, symptoms=?, room_number=?,
                fee_amount=?, reason=?, notes=? WHERE id=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointment.getProviderId());
            ps.setString(2, appointment.getProviderType().name());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setString(4, appointment.getTimeSlot());
            ps.setString(5, appointment.getStatus().name());
            ps.setString(6, appointment.getAppointmentType().name());
            ps.setString(7, appointment.getPriority().name());
            ps.setString(8, appointment.getSymptoms());
            ps.setString(9, appointment.getRoomNumber());
            ps.setBigDecimal(10, appointment.getFeeAmount());
            ps.setString(11, appointment.getReason());
            ps.setString(12, appointment.getNotes());
            ps.setInt(13, appointment.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment", e);
        }
    }

    @Override
    public void updateStatus(int id, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete appointment", e);
        }
    }

    @Override
    public boolean hasConflict(int providerId, LocalDate date, String timeSlot, Integer excludeId) {
        String sql = """
                SELECT COUNT(*) FROM appointments
                WHERE provider_id = ? AND appointment_date = ? AND time_slot = ?
                AND status NOT IN ('CANCELLED')
                """;
        if (excludeId != null) {
            sql += " AND id <> ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, timeSlot);
            if (excludeId != null) {
                ps.setInt(4, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Conflict check failed", e);
        }
    }

    private List<Appointment> queryList(String sql, List<Object> params) {
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof LocalDate ld) {
                    ps.setDate(i + 1, Date.valueOf(ld));
                } else {
                    ps.setObject(i + 1, p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Appointment query failed", e);
        }
        return list;
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
                               String keyword, AppointmentStatus status,
                               LocalDate from, LocalDate to) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (pu.full_name LIKE ? OR pr.full_name LIKE ? OR a.reason LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND a.status = ?");
            params.add(status.name());
        }
        if (from != null) {
            sql.append(" AND a.appointment_date >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND a.appointment_date <= ?");
            params.add(to);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setProviderId(rs.getInt("provider_id"));
        a.setProviderName(rs.getString("provider_name"));
        a.setProviderType(ProviderType.fromString(rs.getString("provider_type")));
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setTimeSlot(rs.getString("time_slot"));
        a.setStatus(AppointmentStatus.fromString(rs.getString("status")));
        mapOptionalColumns(rs, a);
        a.setReason(rs.getString("reason"));
        a.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            a.setCreatedAt(created.toLocalDateTime());
        }
        return a;
    }

    private void mapOptionalColumns(ResultSet rs, Appointment a) throws SQLException {
        try {
            String type = rs.getString("appointment_type");
            if (type != null) {
                a.setAppointmentType(AppointmentType.fromString(type));
            }
            String priority = rs.getString("priority");
            if (priority != null) {
                a.setPriority(Priority.fromString(priority));
            }
            a.setSymptoms(rs.getString("symptoms"));
            a.setRoomNumber(rs.getString("room_number"));
            a.setFeeAmount(rs.getBigDecimal("fee_amount"));
        } catch (SQLException ignored) {
            // legacy schema without new columns
        }
    }
}
