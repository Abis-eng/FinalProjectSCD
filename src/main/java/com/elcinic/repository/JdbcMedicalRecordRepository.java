package com.elcinic.repository;

import com.elcinic.model.MedicalRecord;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMedicalRecordRepository implements MedicalRecordRepository {

    private static final String BASE = """
            SELECT m.*, pu.full_name AS patient_name, du.full_name AS doctor_name
            FROM medical_records m
            JOIN users pu ON m.patient_id = pu.id
            JOIN users du ON m.doctor_id = du.id
            """;

    @Override
    public Optional<MedicalRecord> findById(int id) {
        String sql = BASE + " WHERE m.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find record", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findByPatient(int patientId) {
        return query(BASE + " WHERE m.patient_id = ? ORDER BY m.visit_date DESC", patientId);
    }

    @Override
    public List<MedicalRecord> findByDoctor(int doctorId) {
        return query(BASE + " WHERE m.doctor_id = ? ORDER BY m.visit_date DESC", doctorId);
    }

    @Override
    public List<MedicalRecord> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return query(BASE + " ORDER BY m.visit_date DESC");
        }
        String sql = BASE + """
                 WHERE pu.full_name LIKE ? OR m.diagnosis LIKE ?
                 ORDER BY m.visit_date DESC
                """;
        String like = "%" + keyword.trim() + "%";
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public int create(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, visit_date, diagnosis, prescription, notes) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getPatientId());
            ps.setInt(2, record.getDoctorId());
            ps.setDate(3, Date.valueOf(record.getVisitDate()));
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescription());
            ps.setString(6, record.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create record", e);
        }
    }

    @Override
    public void update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET visit_date=?, diagnosis=?, prescription=?, notes=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(record.getVisitDate()));
            ps.setString(2, record.getDiagnosis());
            ps.setString(3, record.getPrescription());
            ps.setString(4, record.getNotes());
            ps.setInt(5, record.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update record", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM medical_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete record", e);
        }
    }

    private List<MedicalRecord> query(String sql, Object... params) {
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        MedicalRecord m = new MedicalRecord();
        m.setId(rs.getInt("id"));
        m.setPatientId(rs.getInt("patient_id"));
        m.setPatientName(rs.getString("patient_name"));
        m.setDoctorId(rs.getInt("doctor_id"));
        m.setDoctorName(rs.getString("doctor_name"));
        m.setVisitDate(rs.getDate("visit_date").toLocalDate());
        m.setDiagnosis(rs.getString("diagnosis"));
        m.setPrescription(rs.getString("prescription"));
        m.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            m.setCreatedAt(created.toLocalDateTime());
        }
        return m;
    }
}
