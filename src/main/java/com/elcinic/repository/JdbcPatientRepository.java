package com.elcinic.repository;

import com.elcinic.model.PatientProfile;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPatientRepository implements PatientRepository {

    @Override
    public Optional<PatientProfile> findByUserId(int userId) {
        String sql = """
                SELECT p.*, u.full_name AS patient_name, u.email, u.phone,
                       d.full_name AS doctor_name, rd.full_name AS requested_doctor_name
                FROM patients p
                JOIN users u ON p.user_id = u.id
                LEFT JOIN users d ON p.assigned_doctor_id = d.id
                LEFT JOIN users rd ON p.requested_doctor_id = rd.id
                WHERE p.user_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load patient profile", e);
        }
        return Optional.empty();
    }

    @Override
    public List<PatientProfile> findAll(String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, u.full_name AS patient_name, u.email, u.phone,
                       d.full_name AS doctor_name, rd.full_name AS requested_doctor_name
                FROM patients p
                JOIN users u ON p.user_id = u.id
                LEFT JOIN users d ON p.assigned_doctor_id = d.id
                LEFT JOIN users rd ON p.requested_doctor_id = rd.id
                WHERE u.active = 1 AND u.account_status = 'ACTIVE'
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND u.full_name LIKE ?");
        }
        sql.append(" ORDER BY u.full_name");

        List<PatientProfile> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(1, "%" + keyword.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientProfile p = mapRow(rs);
                    p.setAssignedDoctorName(rs.getString("doctor_name"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list patients", e);
        }
        return list;
    }

    @Override
    public void create(int userId, PatientProfile profile) {
        String sql = "INSERT INTO patients (user_id, date_of_birth, blood_type, assigned_doctor_id, requested_doctor_id) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            setDate(ps, 2, profile.getDateOfBirth());
            ps.setString(3, profile.getBloodType());
            if (profile.getAssignedDoctorId() != null) {
                ps.setInt(4, profile.getAssignedDoctorId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (profile.getRequestedDoctorId() != null) {
                ps.setInt(5, profile.getRequestedDoctorId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create patient profile", e);
        }
    }

    @Override
    public void update(PatientProfile profile) {
        String sql = "UPDATE patients SET date_of_birth=?, blood_type=?, assigned_doctor_id=?, requested_doctor_id=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setDate(ps, 1, profile.getDateOfBirth());
            ps.setString(2, profile.getBloodType());
            if (profile.getAssignedDoctorId() != null) {
                ps.setInt(3, profile.getAssignedDoctorId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (profile.getRequestedDoctorId() != null) {
                ps.setInt(4, profile.getRequestedDoctorId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, profile.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update patient", e);
        }
    }

    @Override
    public List<PatientProfile> findByAssignedDoctor(int doctorId, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, u.full_name AS patient_name, u.email, u.phone,
                       d.full_name AS doctor_name, rd.full_name AS requested_doctor_name
                FROM patients p
                JOIN users u ON p.user_id = u.id
                LEFT JOIN users d ON p.assigned_doctor_id = d.id
                LEFT JOIN users rd ON p.requested_doctor_id = rd.id
                WHERE u.active = 1 AND u.account_status = 'ACTIVE' AND p.assigned_doctor_id = ?
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND u.full_name LIKE ?");
        }
        sql.append(" ORDER BY u.full_name");

        List<PatientProfile> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, doctorId);
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(2, "%" + keyword.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientProfile p = mapRow(rs);
                    p.setAssignedDoctorName(rs.getString("doctor_name"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list doctor patients", e);
        }
        return list;
    }

    @Override
    public void assignDoctor(int patientUserId, Integer doctorId) {
        String sql = "UPDATE patients SET assigned_doctor_id = ?, requested_doctor_id = NULL WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (doctorId != null) {
                ps.setInt(1, doctorId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, patientUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign doctor", e);
        }
    }

    @Override
    public void requestDoctor(int patientUserId, Integer doctorId) {
        String sql = "UPDATE patients SET requested_doctor_id = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (doctorId != null) {
                ps.setInt(1, doctorId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, patientUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save requested doctor", e);
        }
    }

    private PatientProfile mapRow(ResultSet rs) throws SQLException {
        PatientProfile p = new PatientProfile();
        p.setUserId(rs.getInt("user_id"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            p.setDateOfBirth(dob.toLocalDate());
        }
        p.setBloodType(rs.getString("blood_type"));
        int docId = rs.getInt("assigned_doctor_id");
        if (!rs.wasNull()) {
            p.setAssignedDoctorId(docId);
        }
        int reqDocId = rs.getInt("requested_doctor_id");
        if (!rs.wasNull()) {
            p.setRequestedDoctorId(reqDocId);
        }
        try {
            p.setFullName(rs.getString("patient_name"));
            p.setEmail(rs.getString("email"));
            p.setPhone(rs.getString("phone"));
            p.setAssignedDoctorName(rs.getString("doctor_name"));
            p.setRequestedDoctorName(rs.getString("requested_doctor_name"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date != null) {
            ps.setDate(index, Date.valueOf(date));
        } else {
            ps.setNull(index, Types.DATE);
        }
    }
}
