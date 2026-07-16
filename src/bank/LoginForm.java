package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Login");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152, 152, 152)); 

                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(40, 40, 100, 25);
        userLabel.setForeground(Color.BLACK);
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(140, 40, 150, 25);
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(40, 80, 100, 25);
        passLabel.setForeground(Color.BLACK);
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(140, 80, 150, 25);
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(40, 130, 100, 30);
        loginBtn.setBackground(new Color(173, 216, 230));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> login());
        panel.add(loginBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(190, 130, 100, 30);
        registerBtn.setBackground(new Color(173, 216, 230));
        registerBtn.setForeground(Color.BLACK);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });
        panel.add(registerBtn);

        add(panel);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, password FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPass = rs.getString("password");
                int userId = rs.getInt("user_id");
                if (storedPass.equals(password)) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new Dashboard(userId).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
