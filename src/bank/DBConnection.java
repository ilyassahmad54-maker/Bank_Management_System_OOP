package bank;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "root";
    private static final String PASSWORD = "new_password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Save utility bill payment
    public static boolean saveUtilityBill(UtilityBill bill) {
        String query = "INSERT INTO utility_bills (user_id, bill_type, amount, due_date, paid, payment_date) " +
                       "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bill.getUserId());
            stmt.setString(2, bill.getBillType());
            stmt.setDouble(3, bill.getAmount());
            stmt.setString(4, bill.getDueDate());
            stmt.setBoolean(5, true);  // paid = true on payment

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
