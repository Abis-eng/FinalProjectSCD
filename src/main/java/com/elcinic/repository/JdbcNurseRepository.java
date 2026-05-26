package com.elcinic.repository;

import com.elcinic.model.NurseProfile;
import com.elcinic.utility.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcNurseRepository implements NurseRepository {

    @Override
    public Optional<NurseProfile> findByUserId(int userId) {
        String sql = """
                SELECT n.*, u.full_name
                FROM nurses n JOIN users u ON n.user_id = u.id
                WHERE n.user_id = ? AND u.active = 1 AND u.account_status = 'ACTIVE'
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
            throw new RuntimeException("Failed to load nurse", e);
        }
        return Optional.empty();
    }

    @Override
    public List<NurseProfile> findAll(String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT n.*, u.full_name
                FROM nurses n JOIN users u ON n.user_id = u.id
                WHERE u.active = 1 AND u.account_status = 'ACTIVE'
                """);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR n.department LIKE ?)");
        }
        sql.append(" ORDER BY u.full_name");

        List<NurseProfile> list = new ArrayList<>();
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
            throw new RuntimeException("Failed to list nurses", e);
        }
        return list;
    }

    @Override
    public void create(int userId, NurseProfile profile) {
        String sql = "INSERT INTO nurses (user_id, department) VALUES (?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, profile.getDepartment());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create nurse profile", e);
        }
    }

    private NurseProfile mapRow(ResultSet rs) throws SQLException {
        NurseProfile n = new NurseProfile();
        n.setUserId(rs.getInt("user_id"));
        n.setDepartment(rs.getString("department"));
        n.setFullName(rs.getString("full_name"));
        return n;
    }
}
