package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Dashboard extends JFrame {

    private final int userId;
    private int accountId = -1;
    private JLabel balanceLabel, accountTypeLabel, ageLabel, cnicLabel, addressLabel;

    public Dashboard(int userId) {
        this.userId = userId;
        setTitle("Dashboard");
        setSize(430, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JMenuBar bar = new JMenuBar();
        JMenu loanMenu = new JMenu("Loan");
        JMenuItem applyLoan = new JMenuItem("Apply for Loan");
        loanMenu.add(applyLoan);
        bar.add(loanMenu);
        setJMenuBar(bar);

        applyLoan.addActionListener(e -> {
            if (accountId == -1) {
                JOptionPane.showMessageDialog(this, "No account found. Please create an account first.");
            } else {
                new LoanApplicationForm(userId, accountId).setVisible(true);
            }
        });

        JPanel p = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152, 152, 152));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        balanceLabel = addLabel(p, "Balance: 0.00", 30, 20);
        accountTypeLabel = addLabel(p, "Account: –", 30, 50);
        ageLabel = addLabel(p, "Age: –", 30, 80);
        cnicLabel = addLabel(p, "CNIC: –", 30, 110);
        addressLabel = addLabel(p, "Address: –", 30, 140);

        JButton create = addButton(p, "Create Account", 30, 180, e -> createAccount());
        JButton deposit = addButton(p, "Deposit", 220, 180, e -> deposit());
        JButton withdraw = addButton(p, "Withdraw", 30, 230, e -> withdraw());
        JButton history = addButton(p, "Transaction History", 220, 230, e -> viewTransactions());

        // ✅ Utility bill buttons
        JButton payBill = addButton(p, "Pay Utility Bill", 30, 280, e -> payUtilityBill());
        JButton viewBillHistory = addButton(p, "Bill Payment History", 220, 280, e -> viewBillHistory());

        add(p);
        loadUserInfo();
        updateBalance();
    }

    private JLabel addLabel(JPanel p, String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setBounds(x, y, 350, 25);
        p.add(l);
        return l;
    }

    private JButton addButton(JPanel p, String txt, int x, int y, java.awt.event.ActionListener a) {
        JButton b = new JButton(txt);
        b.setBounds(x, y, 160, 30);
        b.setBackground(new Color(173, 216, 230));
        b.setFocusPainted(false);
        b.addActionListener(a);
        p.add(b);
        return b;
    }

    private void loadUserInfo() {
        String sql = """
            SELECT u.age,u.cnic,u.address,
                   a.account_id,a.account_type,a.balance
            FROM users u
            LEFT JOIN accounts a ON a.user_id=u.user_id
            WHERE u.user_id=? LIMIT 1""";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ageLabel.setText("Age: " + rs.getInt("age"));
                cnicLabel.setText("CNIC: " + rs.getString("cnic"));
                addressLabel.setText("Address: " + rs.getString("address"));
                accountTypeLabel.setText("Account: " + rs.getString("account_type"));
                accountId = rs.getInt("account_id");
                balanceLabel.setText("Balance: " + rs.getBigDecimal("balance"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateBalance() {
        if (accountId == -1) {
            balanceLabel.setText("Balance: 0.00");
            return;
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT balance FROM accounts WHERE account_id=?")) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                balanceLabel.setText("Balance: " + rs.getBigDecimal(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createAccount() {
        String accType = "Savings";
        try (Connection c = DBConnection.getConnection()) {
            String chk = "SELECT 1 FROM accounts WHERE user_id=? AND account_type=?";
            try (PreparedStatement ps = c.prepareStatement(chk)) {
                ps.setInt(1, userId);
                ps.setString(2, accType);
                if (ps.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this,
                            "You already have a " + accType + " account.");
                    return;
                }
            }

            String accNum = "AC" + userId + "_" + System.currentTimeMillis();
            String ins = """
                INSERT INTO accounts (user_id,account_type,account_number,balance)
                VALUES (?,?,?,0.00)""";
            try (PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, accType);
                ps.setString(3, accNum);
                ps.executeUpdate();
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) accountId = gk.getInt(1);
            }

            JOptionPane.showMessageDialog(this,
                    "New " + accType + " account created!\nNumber: " + accNum);
            loadUserInfo();
            updateBalance();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }

    private void deposit() {
        if (accountId == -1) {
            JOptionPane.showMessageDialog(this, "Create an account first.");
            return;
        }
        String s = JOptionPane.showInputDialog(this, "Amount to deposit:");
        if (s == null) return;
        double amt;
        try {
            amt = Double.parseDouble(s);
            if (amt <= 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid amount");
            return;
        }

        String upd = "UPDATE accounts SET balance=balance+? WHERE account_id=?";
        String log = "INSERT INTO transactions (account_id,type,amount,description) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setDouble(1, amt);
                ps.setInt(2, accountId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(log)) {
                ps.setInt(1, accountId);
                ps.setString(2, "Deposit");
                ps.setDouble(3, amt);
                ps.setString(4, "Cash deposit");
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Deposit successful!");
            updateBalance();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error");
        }
    }

    private void withdraw() {
        if (accountId == -1) {
            JOptionPane.showMessageDialog(this, "Create an account first.");
            return;
        }
        String s = JOptionPane.showInputDialog(this, "Amount to withdraw:");
        if (s == null) return;
        double amt;
        try {
            amt = Double.parseDouble(s);
            if (amt <= 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid amount");
            return;
        }

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            double bal = 0;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id=? FOR UPDATE")) {
                ps.setInt(1, accountId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) bal = rs.getDouble(1);
            }

            if (bal < amt) {
                c.rollback();
                JOptionPane.showMessageDialog(this, "Insufficient balance");
                return;
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE accounts SET balance=balance-? WHERE account_id=?")) {
                ps.setDouble(1, amt);
                ps.setInt(2, accountId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO transactions (account_id,type,amount,description) VALUES (?,?,?,?)")) {
                ps.setInt(1, accountId);
                ps.setString(2, "Withdrawal");
                ps.setDouble(3, amt);
                ps.setString(4, "ATM cash out");
                ps.executeUpdate();
            }
            c.commit();
            JOptionPane.showMessageDialog(this, "Withdrawal successful!");
            updateBalance();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }

    private void viewTransactions() {
        if (accountId == -1) {
            JOptionPane.showMessageDialog(this, "No account yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        String q = "SELECT ts,type,amount,description FROM transactions WHERE account_id=? ORDER BY ts DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append(rs.getTimestamp("ts"))
                        .append(" - ")
                        .append(rs.getString("type"))
                        .append(" : ")
                        .append(rs.getBigDecimal("amount"))
                        .append("  (").append(rs.getString("description")).append(")\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTextArea ta = new JTextArea(sb.length() == 0 ? "No transactions" : sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Transaction History",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void payUtilityBill() {
        new UtilityBillPaymentForm(userId).setVisible(true);
    }

    private void viewBillHistory() {
        new BillPaymentHistoryForm(userId).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard(1).setVisible(true));
    }
}
