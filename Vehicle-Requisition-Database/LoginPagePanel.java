// LoginPagePanel.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoginPagePanel extends JPanel {

    private final MainPortal main;

    private JComboBox<String> roleCombo;
    private JTextField userField;
    private JPasswordField passField;

    public LoginPagePanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(235, 242, 248));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(235, 242, 248));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("VEHICLE REQUISITION PORTAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 70, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 40, 10);
        mainPanel.add(title, gbc);

        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        loginCard.setPreferredSize(new Dimension(400, 350));

        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.insets = new Insets(10, 10, 10, 10);
        gbcCard.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginTitle = new JLabel("Login", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginTitle.setForeground(new Color(0, 70, 140));
        gbcCard.gridx = 0;
        gbcCard.gridy = 0;
        gbcCard.gridwidth = 2;
        gbcCard.insets = new Insets(0, 0, 20, 0);
        loginCard.add(loginTitle, gbcCard);

        // Role
        gbcCard.gridy = 1;
        gbcCard.gridwidth = 1;
        gbcCard.insets = new Insets(5, 5, 5, 5);

        JLabel roleLabel = new JLabel("Login As:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginCard.add(roleLabel, gbcCard);

        gbcCard.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"Admin", "Employee", "Garage", "HOD", "COO"});
        styleCombo(roleCombo);
        loginCard.add(roleCombo, gbcCard);

        // Username
        gbcCard.gridx = 0;
        gbcCard.gridy = 2;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginCard.add(userLabel, gbcCard);

        gbcCard.gridx = 1;
        userField = new JTextField();
        styleField(userField);
        userField.setPreferredSize(new Dimension(200, 30));
        loginCard.add(userField, gbcCard);

        // Password
        gbcCard.gridx = 0;
        gbcCard.gridy = 3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginCard.add(passLabel, gbcCard);

        gbcCard.gridx = 1;
        passField = new JPasswordField();
        styleField(passField);
        passField.setPreferredSize(new Dimension(200, 30));
        loginCard.add(passField, gbcCard);

        // Login button
        gbcCard.gridx = 0;
        gbcCard.gridy = 4;
        gbcCard.gridwidth = 2;
        gbcCard.insets = new Insets(20, 5, 5, 5);

        JButton loginBtn = new JButton("LOGIN");
        stylePrimaryButton(loginBtn);
        loginBtn.setPreferredSize(new Dimension(200, 40));
        loginCard.add(loginBtn, gbcCard);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(loginCard, gbc);

        add(mainPanel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> doLogin());
        loginBtn.setMnemonic(KeyEvent.VK_ENTER);
        passField.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String role = roleCombo.getSelectedItem().toString();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        try {
            // Fully DB-based authentication
            String[] userData = DatabaseOperations.authenticateUser(user, pass);
            if (userData != null) {
                // userData[0] = emp_id, userData[5] = role from DB
                main.setCurrentEmployeeId(userData[0]);
                main.setCurrentRole(userData[5]);

                // Ensure selected UI role matches DB role
                if (!role.equalsIgnoreCase(userData[5])) {
                    JOptionPane.showMessageDialog(this,
                            "Selected role does not match your account.\nYour role: " + userData[5],
                            "Role Mismatch", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                main.setGarageLoggedIn("Garage".equals(userData[5]));

                switch (userData[5]) {
                    case "Admin":
                        main.setGarageLoggedIn(false);
                        main.showPage("ADMIN_DASH");
                        break;

                    case "Employee":
                        main.setGarageLoggedIn(false);
                        main.showPage("EMP_DASH");
                        break;

                    case "Garage":
                        main.setGarageLoggedIn(true);
                        main.showPage("GARAGE_PENDING");
                        break;

                    case "HOD":
                        main.setGarageLoggedIn(false);
                        main.showPage("HOD_DASH");
                        break;

                    case "COO":
                        main.setGarageLoggedIn(false);
                        main.showPage("COO_DASH");
                        break;

                    default:
                        JOptionPane.showMessageDialog(this,
                                "Unknown role: " + userData[5],
                                "Login Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            // If you want absolutely no non-DB fallback, just show DB error:
            JOptionPane.showMessageDialog(this,
                    "Unable to connect to database.\n" + e.getMessage(),
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 120, 215));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
