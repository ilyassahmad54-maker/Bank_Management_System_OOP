package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BillPaymentHistoryForm extends JFrame {
    private final int userId;

    public BillPaymentHistoryForm(int userId) {
        this.userId = userId;
        setTitle("Utility Bill Payment History");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Custom panel with background color
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152, 152, 152)); // grey background
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(new Color(230, 230, 230));
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);

        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel);

        loadBillHistory(area);
    }

    private void loadBillHistory(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        String sql = """
            SELECT bill_type, amount, status, due_date, ts
            FROM utility_bills
            WHERE user_id = ?
            ORDER BY ts DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sb.append("Date: ").append(rs.getTimestamp("ts"))
                  .append("\nBill Type: ").append(rs.getString("bill_type"))
                  .append("\nAmount: ").append(rs.getBigDecimal("amount"))
                  .append("\nStatus: ").append(rs.getString("status"))
                  .append("\nDue Date: ").append(rs.getDate("due_date"))
                  .append("\n-----------------------------\n");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            sb.append("Error fetching history: ").append(ex.getMessage());
        }

        area.setText(sb.length() == 0 ? "No bill payments found." : sb.toString());
    }
}
