package com.elcinic.repository;

import com.elcinic.model.ChatMessage;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcChatRepository implements ChatRepository {

    @Override
    public List<ChatMessage> findThread(int userA, int userB, Integer appointmentId) {
        String sql = """
                SELECT c.*, su.full_name AS sender_name, ru.full_name AS receiver_name
                FROM chat_messages c
                JOIN users su ON c.sender_id = su.id
                JOIN users ru ON c.receiver_id = ru.id
                WHERE ((c.sender_id = ? AND c.receiver_id = ?) OR (c.sender_id = ? AND c.receiver_id = ?))
                AND ((? IS NULL AND c.appointment_id IS NULL) OR c.appointment_id = ?)
                ORDER BY c.created_at ASC
                """;
        List<ChatMessage> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            if (appointmentId == null) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(5, appointmentId);
                ps.setInt(6, appointmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load chat thread", e);
        }
        return list;
    }

    @Override
    public int create(ChatMessage message) {
        String sql = """
                INSERT INTO chat_messages (sender_id, receiver_id, appointment_id, content, is_read)
                VALUES (?,?,?,?,0)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            if (message.getAppointmentId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, message.getAppointmentId());
            }
            ps.setString(4, message.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to send chat message", e);
        }
    }

    @Override
    public void markThreadRead(int currentUserId, int otherUserId, Integer appointmentId) {
        String sql = """
                UPDATE chat_messages
                SET is_read = 1
                WHERE sender_id = ? AND receiver_id = ?
                AND ((? IS NULL AND appointment_id IS NULL) OR appointment_id = ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otherUserId);
            ps.setInt(2, currentUserId);
            if (appointmentId == null) {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(3, appointmentId);
                ps.setInt(4, appointmentId);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark messages as read", e);
        }
    }

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        int aid = rs.getInt("appointment_id");
        if (!rs.wasNull()) {
            m.setAppointmentId(aid);
        }
        m.setContent(rs.getString("content"));
        m.setRead(rs.getInt("is_read") == 1);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            m.setCreatedAt(ts.toLocalDateTime());
        }
        m.setSenderName(rs.getString("sender_name"));
        m.setReceiverName(rs.getString("receiver_name"));
        return m;
    }
}
