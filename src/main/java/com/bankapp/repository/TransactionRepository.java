package com.bankapp.repository;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private static final Logger log = LoggerFactory.getLogger(TransactionRepository.class);

    /**
     * Logs a transaction record. Must be called within an active DB connection/transaction.
     */
    public void log(Connection conn, int accountId, String type, long amountCents, String description) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setLong(3, amountCents);
            ps.setString(4, description);
            ps.executeUpdate();
        }
    }

    /** Returns the N most recent transactions for an account, newest first. */
    public List<Transaction> findRecentByAccountId(int accountId, int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find recent transactions for account_id {}", accountId, e);
        }
        return list;
    }

    /** Returns all transactions for an account, optionally filtered by type. */
    public List<Transaction> findByAccountId(int accountId, String typeFilter) {
        List<Transaction> list = new ArrayList<>();
        boolean hasFilter = typeFilter != null && !typeFilter.isBlank() && !typeFilter.equalsIgnoreCase("All");
        String sql = hasFilter
                ? "SELECT * FROM transactions WHERE account_id = ? AND type = ? ORDER BY timestamp DESC"
                : "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            if (hasFilter) ps.setString(2, typeFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find transactions for account_id {}", accountId, e);
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return Transaction.builder()
                .transactionId(rs.getInt("transaction_id"))
                .accountId(rs.getInt("account_id"))
                .type(rs.getString("type"))
                .amountCents(rs.getLong("amount"))
                .description(rs.getString("description"))
                .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
                .build();
    }
}
