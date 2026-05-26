package com.elcinic.repository;

import com.elcinic.model.PrescriptionItem;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcPrescriptionRepository implements PrescriptionRepository {

    @Override
    public List<PrescriptionItem> findByMedicalRecord(int recordId) {
        String sql = "SELECT * FROM prescription_items WHERE medical_record_id = ? ORDER BY id";
        List<PrescriptionItem> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, recordId);
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

    @Override
    public void create(PrescriptionItem item) {
        String sql = """
                INSERT INTO prescription_items
                (medical_record_id, medication_name, dosage, frequency, duration_days, instructions)
                VALUES (?,?,?,?,?,?)
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, item.getMedicalRecordId());
            ps.setString(2, item.getMedicationName());
            ps.setString(3, item.getDosage());
            ps.setString(4, item.getFrequency());
            ps.setInt(5, item.getDurationDays());
            ps.setString(6, item.getInstructions());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByRecord(int recordId) {
        String sql = "DELETE FROM prescription_items WHERE medical_record_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PrescriptionItem map(ResultSet rs) throws SQLException {
        PrescriptionItem p = new PrescriptionItem();
        p.setId(rs.getInt("id"));
        p.setMedicalRecordId(rs.getInt("medical_record_id"));
        p.setMedicationName(rs.getString("medication_name"));
        p.setDosage(rs.getString("dosage"));
        p.setFrequency(rs.getString("frequency"));
        p.setDurationDays(rs.getInt("duration_days"));
        p.setInstructions(rs.getString("instructions"));
        return p;
    }
}
