package com.bankapp.repository;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class UserRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    /** Inserts a new user. Returns the generated user_id, or -1 on failure. */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, age, cnic, address, phone) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getAge());
            ps.setString(4, user.getCnic());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Failed to insert user '{}'", user.getUsername(), e);
        }
        return -1;
    }

    /** Finds a user by username. Returns Optional.empty() if not found. */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find user '{}'", username, e);
        }
        return Optional.empty();
    }

    /** Finds a user by user_id. */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find user by id {}", userId, e);
        }
        return Optional.empty();
    }

    /** Updates user profile fields. */
    public boolean update(User user) {
        String sql = "UPDATE users SET age=?, cnic=?, address=?, phone=? WHERE user_id=?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getAge());
            ps.setString(2, user.getCnic());
            ps.setString(3, user.getAddress());
            ps.setString(4, user.getPhone());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to update user id {}", user.getUserId(), e);
        }
        return false;
    }

    /** Updates user password hash. */
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to update password for user id {}", userId, e);
        }
        return false;
    }

    /** Checks if a username is already taken. */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Failed to check username existence '{}'", username, e);
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .userId(rs.getInt("user_id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .age(rs.getInt("age"))
                .cnic(rs.getString("cnic"))
                .address(rs.getString("address"))
                .phone(rs.getString("phone"))
                .build();
    }
}
