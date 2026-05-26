package com.elcinic.repository;

import com.elcinic.model.LabStatus;
import com.elcinic.model.LabTest;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLabTestRepository implements LabTestRepository {

    private static final String BASE = """
            SELECT l.*, pu.full_name AS patient_name, du.full_name AS doctor_name
            FROM lab_tests l
            JOIN users pu ON l.patient_id = pu.id
            JOIN users du ON l.doctor_id = du.id
            """;

    @Override
    public Optional<LabTest> findById(int id) {
        List<LabTest> list = query(BASE + " WHERE l.id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<LabTest> findByPatient(int patientId) {
        return query(BASE + " WHERE l.patient_id = ? ORDER BY l.ordered_date DESC", patientId);
    }

    @Override
    public List<LabTest> findByDoctor(int doctorId) {
        return query(BASE + " WHERE l.doctor_id = ? ORDER BY l.ordered_date DESC", doctorId);
    }

    @Override
    public List<LabTest> findAll(String keyword, LabStatus status) {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (pu.full_name LIKE ? OR l.test_name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND l.status = ?");
            params.add(status.name());
        }
        sql.append(" ORDER BY l.ordered_date DESC");
        return query(sql.toString(), params.toArray());
    }

    @Override
    public int create(LabTest test) {
        String sql = """
                INSERT INTO lab_tests (patient_id, doctor_id, test_name, status, ordered_date, notes)
                VALUES (?,?,?,?,?,?)
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, test.getPatientId());
            ps.setInt(2, test.getDoctorId());
            ps.setString(3, test.getTestName());
            ps.setString(4, test.getStatus().name());
            ps.setDate(5, Date.valueOf(test.getOrderedDate()));
            ps.setString(6, test.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(LabTest test) {
        String sql = """
                UPDATE lab_tests SET result_value=?, result_unit=?, status=?, completed_date=?, notes=?
                WHERE id=?
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, test.getResultValue());
            ps.setString(2, test.getResultUnit());
            ps.setString(3, test.getStatus().name());
            if (test.getCompletedDate() != null) {
                ps.setDate(4, Date.valueOf(test.getCompletedDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, test.getNotes());
            ps.setInt(6, test.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM lab_tests WHERE status IN ('ORDERED','IN_PROGRESS')";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<LabTest> query(String sql, Object... params) {
        List<LabTest> list = new ArrayList<>();
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

    private LabTest map(ResultSet rs) throws SQLException {
        LabTest l = new LabTest();
        l.setId(rs.getInt("id"));
        l.setPatientId(rs.getInt("patient_id"));
        l.setPatientName(rs.getString("patient_name"));
        l.setDoctorId(rs.getInt("doctor_id"));
        l.setDoctorName(rs.getString("doctor_name"));
        l.setTestName(rs.getString("test_name"));
        l.setResultValue(rs.getString("result_value"));
        l.setResultUnit(rs.getString("result_unit"));
        l.setStatus(LabStatus.fromString(rs.getString("status")));
        Date od = rs.getDate("ordered_date");
        if (od != null) {
            l.setOrderedDate(od.toLocalDate());
        }
        Date cd = rs.getDate("completed_date");
        if (cd != null) {
            l.setCompletedDate(cd.toLocalDate());
        }
        l.setNotes(rs.getString("notes"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) {
            l.setCreatedAt(t.toLocalDateTime());
        }
        return l;
    }
}
