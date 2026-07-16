package com.bankapp.repository;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {
    private static final Logger log = LoggerFactory.getLogger(AccountRepository.class);

    public int insert(Account account) {
        String sql = "INSERT INTO accounts (user_id, account_type, account_number, balance) VALUES (?,?,?,?)";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getAccountType());
            ps.setString(3, account.getAccountNumber());
            ps.setLong(4, account.getBalanceCents());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Failed to insert account for user_id {}", account.getUserId(), e);
        }
        return -1;
    }

    public Optional<Account> findPrimaryByUserId(int userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY account_id ASC LIMIT 1";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find primary account for user_id {}", userId, e);
        }
        return Optional.empty();
    }

    public List<Account> findAllByUserId(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY account_id ASC";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find accounts for user_id {}", userId, e);
        }
        return list;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find account by number '{}'", accountNumber, e);
        }
        return Optional.empty();
    }

    public Optional<Account> findById(int accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find account by id {}", accountId, e);
        }
        return Optional.empty();
    }

    /**
     * Atomically deposits an amount (in cents) into an account.
     * Must be called inside an existing transaction if chaining operations.
     */
    public boolean deposit(Connection conn, int accountId, long amountCents) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, amountCents);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Atomically withdraws an amount (in cents). Checks for sufficient funds first.
     * Returns false if insufficient funds (does NOT throw).
     * Must be called inside an existing transaction.
     */
    public boolean withdraw(Connection conn, int accountId, long amountCents) throws SQLException {
        // Lock the row and check balance
        String checkSql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long balance = rs.getLong("balance");
                    if (balance < amountCents) return false; // insufficient funds
                }
            }
        }
        String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setLong(1, amountCents);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Gets the current balance in cents for an account. */
    public long getBalance(int accountId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("balance");
            }
        } catch (SQLException e) {
            log.error("Failed to get balance for account_id {}", accountId, e);
        }
        return 0L;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        return Account.builder()
                .accountId(rs.getInt("account_id"))
                .userId(rs.getInt("user_id"))
                .accountType(rs.getString("account_type"))
                .accountNumber(rs.getString("account_number"))
                .balanceCents(rs.getLong("balance"))
                .build();
    }
}
