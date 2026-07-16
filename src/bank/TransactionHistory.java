package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TransactionHistory extends JFrame {
    public TransactionHistory(int userId) {
        setTitle("Transaction History");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

       
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
        area.setBackground(new Color(230, 230, 230)); // Light grey text background
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.type, t.amount, t.timestamp FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.account_id WHERE a.user_id = ?"
            );
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                area.append(
                    rs.getString("type") + " - " +
                    rs.getDouble("amount") + " at " +
                    rs.getTimestamp("timestamp") + "\n"
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            area.setText("Error loading transaction history: " + ex.getMessage());
        }

        JScrollPane scroll = new JScrollPane(area);
        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel);
    }
}
