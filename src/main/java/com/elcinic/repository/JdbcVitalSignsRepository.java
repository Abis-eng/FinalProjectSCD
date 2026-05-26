package com.elcinic.repository;

import com.elcinic.model.VitalSigns;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class JdbcVitalSignsRepository implements VitalSignsRepository {

    @Override
    public Optional<VitalSigns> findByAppointment(int appointmentId) {
        String sql = """
                SELECT v.*, u.full_name AS recorded_by_name
                FROM vital_signs v
                LEFT JOIN users u ON v.recorded_by = u.id
                WHERE v.appointment_id = ?
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void save(VitalSigns v) {
        Optional<VitalSigns> existing = findByAppointment(v.getAppointmentId());
        if (existing.isPresent()) {
            String sql = """
                    UPDATE vital_signs SET blood_pressure=?, pulse=?, temperature=?,
                    weight_kg=?, height_cm=?, recorded_by=? WHERE appointment_id=?
                    """;
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, v);
                ps.setInt(7, v.getAppointmentId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            String sql = """
                    INSERT INTO vital_signs (appointment_id, blood_pressure, pulse, temperature,
                    weight_kg, height_cm, recorded_by) VALUES (?,?,?,?,?,?,?)
                    """;
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, v.getAppointmentId());
                bind(ps, v, 2);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void bind(PreparedStatement ps, VitalSigns v) throws SQLException {
        bind(ps, v, 1);
    }

    private void bind(PreparedStatement ps, VitalSigns v, int start) throws SQLException {
        ps.setString(start, v.getBloodPressure());
        if (v.getPulse() != null) {
            ps.setInt(start + 1, v.getPulse());
        } else {
            ps.setNull(start + 1, Types.INTEGER);
        }
        if (v.getTemperature() != null) {
            ps.setDouble(start + 2, v.getTemperature());
        } else {
            ps.setNull(start + 2, Types.DOUBLE);
        }
        if (v.getWeightKg() != null) {
            ps.setDouble(start + 3, v.getWeightKg());
        } else {
            ps.setNull(start + 3, Types.DOUBLE);
        }
        if (v.getHeightCm() != null) {
            ps.setDouble(start + 4, v.getHeightCm());
        } else {
            ps.setNull(start + 4, Types.DOUBLE);
        }
        ps.setInt(start + 5, v.getRecordedBy());
    }

    private VitalSigns map(ResultSet rs) throws SQLException {
        VitalSigns v = new VitalSigns();
        v.setId(rs.getInt("id"));
        v.setAppointmentId(rs.getInt("appointment_id"));
        v.setBloodPressure(rs.getString("blood_pressure"));
        int pulse = rs.getInt("pulse");
        if (!rs.wasNull()) {
            v.setPulse(pulse);
        }
        double temp = rs.getDouble("temperature");
        if (!rs.wasNull()) {
            v.setTemperature(temp);
        }
        double w = rs.getDouble("weight_kg");
        if (!rs.wasNull()) {
            v.setWeightKg(w);
        }
        double h = rs.getDouble("height_cm");
        if (!rs.wasNull()) {
            v.setHeightCm(h);
        }
        v.setRecordedBy(rs.getInt("recorded_by"));
        v.setRecordedByName(rs.getString("recorded_by_name"));
        Timestamp t = rs.getTimestamp("recorded_at");
        if (t != null) {
            v.setRecordedAt(t.toLocalDateTime());
        }
        return v;
    }
}
