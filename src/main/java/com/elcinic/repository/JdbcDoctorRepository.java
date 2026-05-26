package com.elcinic.repository;

import com.elcinic.model.DoctorProfile;
import com.elcinic.utility.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDoctorRepository implements DoctorRepository {

    @Override
    public Optional<DoctorProfile> findByUserId(int userId) {
        String sql = """
                SELECT d.*, u.full_name
                FROM doctors d JOIN users u ON d.user_id = u.id
                WHERE d.user_id = ? AND u.active = 1 AND u.account_status = 'ACTIVE'
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
            throw new RuntimeException("Failed to load doctor", e);
        }
        return Optional.empty();
    }

    @Override
    public List<DoctorProfile> findAll(String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT d.*, u.full_name
                FROM doctors d JOIN users u ON d.user_id = u.id
                WHERE u.active = 1 AND u.account_status = 'ACTIVE'
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR d.specialization LIKE ?)");
        }
        sql.append(" ORDER BY u.full_name");

        List<DoctorProfile> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list doctors", e);
        }
        return list;
    }

    @Override
    public void create(int userId, DoctorProfile profile) {
        String sql = "INSERT INTO doctors (user_id, specialization, license_number, consultation_fee) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, profile.getSpecialization());
            ps.setString(3, profile.getLicenseNumber());
            BigDecimal fee = profile.getConsultationFee() != null
                    ? profile.getConsultationFee() : BigDecimal.valueOf(3000);
            ps.setBigDecimal(4, fee);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create doctor profile", e);
        }
    }

    @Override
    public void updateConsultationFee(int userId, BigDecimal fee) {
        String sql = "UPDATE doctors SET consultation_fee = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, fee);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update consultation fee", e);
        }
    }

    private DoctorProfile mapRow(ResultSet rs) throws SQLException {
        DoctorProfile d = new DoctorProfile();
        d.setUserId(rs.getInt("user_id"));
        d.setSpecialization(rs.getString("specialization"));
        d.setLicenseNumber(rs.getString("license_number"));
        d.setFullName(rs.getString("full_name"));
        try {
            BigDecimal fee = rs.getBigDecimal("consultation_fee");
            if (fee != null) {
                d.setConsultationFee(fee);
            }
        } catch (SQLException ignored) {
        }
        return d;
    }
}
