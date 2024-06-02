package org.library.gui;

import org.library.user.User;
import org.library.user.Role;
import org.library.database.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends JFrame {
    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginPage() {
        setTitle("Library App - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);

        panel = new JPanel(new GridLayout(3, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        panel.add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
        panel.add(registerButton);

        add(panel);
    }

    private void login() {
        String username = usernameField.getText();
        String password = String.valueOf(passwordField.getPassword());

        User user = authenticate(username, password);
        if (user != null) {
            openUserPage(user);
        } else {
            JOptionPane.showMessageDialog(panel, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private User authenticate(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                Role role = Role.valueOf(rs.getString("role"));
                return new User(id, username, password, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showRegisterDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"USER", "ADMIN"});

        JPanel dialogPanel = new JPanel(new GridLayout(3, 2));
        dialogPanel.add(new JLabel("Username:"));
        dialogPanel.add(usernameField);
        dialogPanel.add(new JLabel("Password:"));
        dialogPanel.add(passwordField);
        dialogPanel.add(new JLabel("Role:"));
        dialogPanel.add(roleComboBox);

        int result = JOptionPane.showConfirmDialog(panel, dialogPanel, "Register", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = String.valueOf(passwordField.getPassword());
            Role role = Role.valueOf((String) roleComboBox.getSelectedItem());
            registerUser(username, password, role);
        }
    }

    private void registerUser(String username, String password, Role role) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role.name());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(panel, "User registered successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openUserPage(User user) {
        if (user.getRole() == Role.ADMIN) {
            openAdminPage();
        } else {
            openUserPage();
        }
    }

    private void openAdminPage() {
        AdminPage adminPage = new AdminPage();
        adminPage.setVisible(true);
        dispose();
    }

    private void openUserPage() {
        UserPage userPage = new UserPage();
        userPage.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}
