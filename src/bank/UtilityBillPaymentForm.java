package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class UtilityBillPaymentForm extends JFrame {
    private JComboBox<String> billTypeCombo;
    private JTextField amountField;
    private final int userId;

    public UtilityBillPaymentForm(int userId) {
        this.userId = userId;
        setTitle("Pay Utility Bill");
        setSize(300, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Custom panel with grey background
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152, 152, 152));  // grey background
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel billTypeLabel = new JLabel("Bill Type:");
        billTypeLabel.setBounds(20, 20, 80, 25);
        panel.add(billTypeLabel);

        billTypeCombo = new JComboBox<>(new String[]{"Electricity", "Water", "Gas", "Internet"});
        billTypeCombo.setBounds(110, 20, 150, 25);
        panel.add(billTypeCombo);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(20, 60, 80, 25);
        panel.add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(110, 60, 150, 25);
        panel.add(amountField);

        JButton payButton = new JButton("Pay");
        payButton.setBounds(90, 110, 100, 30);
        payButton.setBackground(new Color(173, 216, 230));
        payButton.setFocusPainted(false);
        payButton.addActionListener(e -> payBill());
        panel.add(payButton);

        add(panel);
    }

    private void payBill() {
        String billType = (String) billTypeCombo.getSelectedItem();
        String amountText = amountField.getText().trim();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
            return;
        }

        // Default due date = 7 days from now
        LocalDate dueDate = LocalDate.now().plusDays(7);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                INSERT INTO utility_bills (user_id, bill_type, amount, status, due_date, payment_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, billType);
            ps.setDouble(3, amount);
            ps.setString(4, "Paid");
            ps.setDate(5, Date.valueOf(dueDate));
            ps.setDate(6, Date.valueOf(LocalDate.now()));  

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Bill paid successfully!");
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}
