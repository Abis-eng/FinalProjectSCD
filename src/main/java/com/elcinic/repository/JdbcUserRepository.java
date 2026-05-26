package com.elcinic.repository;

import com.elcinic.model.*;
import com.elcinic.utility.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND active = 1 AND account_status = 'ACTIVE'";
        return queryOne(sql, username);
    }

    @Override
    public Optional<User> findByUsernameForLogin(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return queryOne(sql, username);
    }

    private Optional<User> queryOne(String sql, String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByRole(Role role) {
        return search(null, role);
    }

    @Override
    public List<User> search(String keyword, Role role) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE active = 1 AND account_status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();
        if (role != null) {
            sql.append(" AND role = ?");
            params.add(role.name());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (full_name LIKE ? OR username LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY full_name");

        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search users", e);
        }
        return users;
    }

    @Override
    public List<PendingStaffRegistration> findPendingStaff() {
        String sql = """
                SELECT u.id AS user_id, u.username, u.full_name, u.email, u.phone, u.role, u.created_at,
                       d.specialization, d.license_number, n.department
                FROM users u
                LEFT JOIN doctors d ON u.id = d.user_id
                LEFT JOIN nurses n ON u.id = n.user_id
                WHERE u.account_status = 'PENDING' AND u.role IN ('DOCTOR','NURSE')
                ORDER BY u.created_at ASC
                """;
        List<PendingStaffRegistration> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PendingStaffRegistration p = new PendingStaffRegistration();
                p.setUserId(rs.getInt("user_id"));
                p.setUsername(rs.getString("username"));
                p.setFullName(rs.getString("full_name"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setRole(Role.fromString(rs.getString("role")));
                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) {
                    p.setRegisteredAt(created.toLocalDateTime());
                }
                p.setSpecialization(rs.getString("specialization"));
                p.setLicenseNumber(rs.getString("license_number"));
                p.setDepartment(rs.getString("department"));
                list.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load pending staff", e);
        }
        return list;
    }

    @Override
    public int create(User user) {
        String sql = """
                INSERT INTO users (username, password_hash, full_name, email, phone, role, active, account_status)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setInt(7, user.isActive() ? 1 : 0);
            ps.setString(8, user.getAccountStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No generated key");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void setActive(int id, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user status", e);
        }
    }

    @Override
    public void updateAccountStatus(int userId, AccountStatus status, Integer verifiedBy, String rejectionReason) {
        String sql = """
                UPDATE users SET account_status = ?, verified_at = ?, verified_by = ?, rejection_reason = ?,
                active = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (status == AccountStatus.ACTIVE) {
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(3, verifiedBy);
                ps.setNull(4, Types.VARCHAR);
                ps.setInt(5, 1);
            } else if (status == AccountStatus.REJECTED) {
                ps.setNull(2, Types.TIMESTAMP);
                ps.setObject(3, verifiedBy);
                ps.setString(4, rejectionReason);
                ps.setInt(5, 0);
            } else {
                ps.setNull(2, Types.TIMESTAMP);
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.VARCHAR);
                ps.setInt(5, 1);
            }
            ps.setInt(6, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account status", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return existsUsernameIncludingInactive(username);
    }

    private boolean existsUsernameIncludingInactive(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setActive(rs.getInt("active") == 1);
        mapAccountStatus(rs, user);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        return user;
    }

    private void mapAccountStatus(ResultSet rs, User user) throws SQLException {
        try {
            String status = rs.getString("account_status");
            if (status != null) {
                user.setAccountStatus(AccountStatus.fromString(status));
            }
            Timestamp verified = rs.getTimestamp("verified_at");
            if (verified != null) {
                user.setVerifiedAt(verified.toLocalDateTime());
            }
            int verifiedBy = rs.getInt("verified_by");
            if (!rs.wasNull()) {
                user.setVerifiedBy(verifiedBy);
            }
            user.setRejectionReason(rs.getString("rejection_reason"));
        } catch (SQLException ignored) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
    }
}
