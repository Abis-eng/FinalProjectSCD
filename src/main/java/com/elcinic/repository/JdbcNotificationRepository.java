package com.elcinic.repository;

import com.elcinic.model.Notification;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcNotificationRepository implements NotificationRepository {

    @Override
    public List<Notification> findByUser(int userId, boolean unreadOnly) {
        String sql = "SELECT * FROM notifications WHERE user_id = ?"
                + (unreadOnly ? " AND is_read = 0" : "")
                + " ORDER BY created_at DESC LIMIT 50";
        List<Notification> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
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
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int create(Notification n) {
        String sql = "INSERT INTO notifications (user_id, title, message) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
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
    public void markRead(int id, int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) {
            n.setCreatedAt(t.toLocalDateTime());
        }
        return n;
    }
}
