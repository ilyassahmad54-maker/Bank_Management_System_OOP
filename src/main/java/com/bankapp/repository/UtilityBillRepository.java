package com.bankapp.repository;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.UtilityBill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilityBillRepository {
    private static final Logger log = LoggerFactory.getLogger(UtilityBillRepository.class);

    public boolean insert(UtilityBill bill) {
        String sql = "INSERT INTO utility_bills (user_id, bill_type, amount, status, due_date, payment_date) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getUserId());
            ps.setString(2, bill.getBillType());
            ps.setLong(3, bill.getAmountCents());
            ps.setString(4, bill.getStatus());
            ps.setString(5, bill.getDueDate() != null ? bill.getDueDate().toString() : null);
            ps.setString(6, bill.getPaymentDate() != null ? bill.getPaymentDate().toString() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to insert utility bill for user_id {}", bill.getUserId(), e);
        }
        return false;
    }

    public List<UtilityBill> findByUserId(int userId) {
        List<UtilityBill> list = new ArrayList<>();
        String sql = "SELECT * FROM utility_bills WHERE user_id = ? ORDER BY ts DESC";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find bills for user_id {}", userId, e);
        }
        return list;
    }

    private UtilityBill mapRow(ResultSet rs) throws SQLException {
        String dueDateStr = rs.getString("due_date");
        String payDateStr = rs.getString("payment_date");
        String tsStr = rs.getString("ts");
        return UtilityBill.builder()
                .billId(rs.getInt("bill_id"))
                .userId(rs.getInt("user_id"))
                .billType(rs.getString("bill_type"))
                .amountCents(rs.getLong("amount"))
                .status(rs.getString("status"))
                .dueDate(dueDateStr != null ? LocalDate.parse(dueDateStr.substring(0, 10)) : null)
                .paymentDate(payDateStr != null ? java.time.LocalDateTime.parse(payDateStr.replace(" ", "T")) : null)
                .createdAt(tsStr != null ? java.time.LocalDateTime.parse(tsStr.replace(" ", "T")) : null)
                .build();
    }
}
