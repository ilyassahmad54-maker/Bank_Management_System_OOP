package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoanApplicationForm extends JFrame {
    private JComboBox<String> loanTypeBox;
    private JTextField principalField, termField, interestField;

    public LoanApplicationForm(int userId, int accountId) {
        setTitle("Loan Application");
        setSize(420, 310);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Custom panel with background color
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152, 152, 152)); // Set background to ash gray
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        add(panel);

        int xLbl = 30, xFld = 170, wLbl = 120, wFld = 180, h = 25, gap = 12;
        int y = 20;

        panel.add(makeLabel("Loan Type:", xLbl, y, wLbl, h));
        loanTypeBox = new JComboBox<>(new String[]{"Personal", "Home", "Auto"});
        loanTypeBox.setBounds(xFld, y, wFld, h);
        panel.add(loanTypeBox);
        y += h + gap;

        panel.add(makeLabel("Principal Amount:", xLbl, y, wLbl, h));
        principalField = new JTextField();
        principalField.setBounds(xFld, y, wFld, h);
        panel.add(principalField);
        y += h + gap;

        panel.add(makeLabel("Interest Rate (%):", xLbl, y, wLbl, h));
        interestField = new JTextField();
        interestField.setBounds(xFld, y, wFld, h);
        panel.add(interestField);
        y += h + gap;

        panel.add(makeLabel("Loan Term (Months):", xLbl, y, wLbl, h));
        termField = new JTextField();
        termField.setBounds(xFld, y, wFld, h);
        panel.add(termField);
        y += h + 2 * gap;

        JButton applyBtn = new JButton("Apply");
        applyBtn.setBounds(140, y, 120, 30);
        panel.add(applyBtn);

        applyBtn.addActionListener(e -> applyLoan(userId, accountId));
    }

    private JLabel makeLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private void applyLoan(int userId, int accountId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO loans (user_id, account_id, loan_type, principal_amount, interest_rate, loan_term_months, outstanding_balance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, accountId);
            ps.setString(3, (String) loanTypeBox.getSelectedItem());

            double principal = Double.parseDouble(principalField.getText());
            double interest = Double.parseDouble(interestField.getText());
            int term = Integer.parseInt(termField.getText());

            ps.setDouble(4, principal);
            ps.setDouble(5, interest);
            ps.setInt(6, term);
            ps.setDouble(7, principal); // initially, the full principal is outstanding

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Loan request submitted!");
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
