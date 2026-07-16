package bank;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterForm extends JFrame {

    private JTextField txtUsername, txtAge, txtCnic, txtAddress;
    private JPasswordField txtPassword;
    private JComboBox<String> accountTypeBox;

    public RegisterForm() {
        setTitle("Register");
        setSize(430, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        /*   MAIN PANEL WITH GREY BACKGROUND */
        JPanel bg = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(152,152,152));   
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        add(bg);

        /* FORM FIELDS   */
        int xLbl = 40, xFld = 160, wLbl = 100, wFld = 180, h = 28, gap = 10;
        int y = 30;

        bg.add(makeLabel("Username:", xLbl,y,wLbl,h));
        txtUsername = makeField(bg,xFld,y,wFld,h);    y+=h+gap;

        bg.add(makeLabel("Password:", xLbl,y,wLbl,h));
        txtPassword = new JPasswordField(); txtPassword.setBounds(xFld,y,wFld,h); bg.add(txtPassword); y+=h+gap;

        bg.add(makeLabel("Age:", xLbl,y,wLbl,h));
        txtAge = makeField(bg,xFld,y,wFld,h);        y+=h+gap;

        bg.add(makeLabel("CNIC:", xLbl,y,wLbl,h));
        txtCnic = makeField(bg,xFld,y,wFld,h);       y+=h+gap;

        bg.add(makeLabel("Address:", xLbl,y,wLbl,h));
        txtAddress = makeField(bg,xFld,y,wFld,h);    y+=h+gap;

        bg.add(makeLabel("Account Type:", xLbl,y,wLbl,h));
        accountTypeBox = new JComboBox<>(new String[]{"Savings","Current"});
        accountTypeBox.setBounds(xFld,y,wFld,h); bg.add(accountTypeBox);          y+=h+2*gap;

        /*   BUTTONS  */
        JButton btnRegister = new JButton("Register");
        JButton btnBack     = new JButton("Back");
        btnRegister.setBounds(90,y,100,32);
        btnBack.setBounds(230,y,100,32);
        bg.add(btnRegister); bg.add(btnBack);

        /*   ACTIONS   */
        btnRegister.addActionListener(e -> registerUser());
        btnBack.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
    }

    /* helper to create labels & text‑fields */
    private JLabel makeLabel(String t,int x,int y,int w,int h){
        JLabel l=new JLabel(t); l.setBounds(x,y,w,h); return l;
    }
    private JTextField makeField(JPanel p,int x,int y,int w,int h){
        JTextField f=new JTextField(); f.setBounds(x,y,w,h); p.add(f); return f;
    }

    /* ----------------  REGISTRATION LOGIC  ---------------- */
    private void registerUser() {

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String ageStr   = txtAge.getText().trim();
        String cnic     = txtCnic.getText().trim();
        String address  = txtAddress.getText().trim();
        String accType  = (String) accountTypeBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || ageStr.isEmpty() ||
            cnic.isEmpty()     || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Please fill in all fields.");
            return;
        }

        int age;
        try { age = Integer.parseInt(ageStr); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,"Age must be a number."); return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            /* insert user */
            String uSql = "INSERT INTO users (username,password,age,cnic,address) VALUES (?,?,?,?,?)";
            PreparedStatement psU = conn.prepareStatement(uSql, Statement.RETURN_GENERATED_KEYS);
            psU.setString(1,username); psU.setString(2,password);
            psU.setInt   (3,age);      psU.setString(4,cnic); psU.setString(5,address);

            if (psU.executeUpdate()==0) { JOptionPane.showMessageDialog(this,"User insert failed"); return; }

            ResultSet gk = psU.getGeneratedKeys(); gk.next();
            int userId = gk.getInt(1);

            /* generate account number */
            String accNum = "AC"+userId+"_"+System.currentTimeMillis();

            /* insert account */
            String aSql = """
               INSERT INTO accounts (user_id,account_type,account_number,balance)
               VALUES (?,?,?,0.00)""";
            PreparedStatement psA = conn.prepareStatement(aSql);
            psA.setInt   (1,userId);
            psA.setString(2,accType);
            psA.setString(3,accNum);

            if (psA.executeUpdate()==0) {
                JOptionPane.showMessageDialog(this,"Account insert failed");
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nAccount Number: "+accNum);
            dispose();
            new LoginForm().setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"DB error: "+ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterForm().setVisible(true));
    }
}
