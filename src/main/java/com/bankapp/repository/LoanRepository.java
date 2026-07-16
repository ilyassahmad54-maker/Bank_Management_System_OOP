package com.bankapp.repository;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanRepository {
    private static final Logger log = LoggerFactory.getLogger(LoanRepository.class);

    public boolean insert(Loan loan) {
        String sql = "INSERT INTO loans (user_id, account_id, loan_type, principal_amount, interest_rate, loan_term_months, outstanding_balance, status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loan.getUserId());
            ps.setInt(2, loan.getAccountId());
            ps.setString(3, loan.getLoanType());
            ps.setLong(4, loan.getPrincipalAmountCents());
            ps.setDouble(5, loan.getInterestRate());
            ps.setInt(6, loan.getLoanTermMonths());
            ps.setLong(7, loan.getOutstandingBalanceCents());
            ps.setString(8, loan.getStatus() != null ? loan.getStatus() : "Under Review");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to insert loan for user_id {}", loan.getUserId(), e);
        }
        return false;
    }

    public List<Loan> findByUserId(int userId) {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find loans for user_id {}", userId, e);
        }
        return list;
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        String tsStr = rs.getString("created_at");
        return Loan.builder()
                .loanId(rs.getInt("loan_id"))
                .userId(rs.getInt("user_id"))
                .accountId(rs.getInt("account_id"))
                .loanType(rs.getString("loan_type"))
                .principalAmountCents(rs.getLong("principal_amount"))
                .interestRate(rs.getDouble("interest_rate"))
                .loanTermMonths(rs.getInt("loan_term_months"))
                .outstandingBalanceCents(rs.getLong("outstanding_balance"))
                .status(rs.getString("status"))
                .createdAt(tsStr != null ? LocalDateTime.parse(tsStr.replace(" ", "T")) : null)
                .build();
    }
}
