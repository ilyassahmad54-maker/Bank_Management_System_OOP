package bank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UtilityBillPayment extends JFrame {
    private JTextField billTypeField, amountField, dueDateField;
    private int userId;

    public UtilityBillPayment(int userId) {
        this.userId = userId;

        setTitle("Pay Utility Bill");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        panel.add(new JLabel("Bill Type:"));
        billTypeField = new JTextField();
        panel.add(billTypeField);

        panel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        panel.add(amountField);

        panel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        panel.add(dueDateField);

        JButton payButton = new JButton("Pay Bill");
        panel.add(payButton);

        JButton cancelButton = new JButton("Cancel");
        panel.add(cancelButton);

        add(panel);

        payButton.addActionListener(e -> payBill());
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void payBill() {
        String billType = billTypeField.getText();
        String dueDate = dueDateField.getText();
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
            return;
        }

        UtilityBill bill = new UtilityBill(userId, billType, amount, dueDate);

        boolean success = DBConnection.saveUtilityBill(bill);

        if (success) {
            JOptionPane.showMessageDialog(this, "Bill Paid Successfully!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Payment Failed!");
        }
    }
}
