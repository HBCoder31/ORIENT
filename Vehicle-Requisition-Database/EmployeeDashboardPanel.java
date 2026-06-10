// EmployeeDashboardPanel.java

import javax.swing.*;
import java.awt.*;

public class EmployeeDashboardPanel extends JPanel {

    private final MainPortal main;

    public EmployeeDashboardPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Top panel with logo
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Back (reload) button – sends to EMP_DASH itself, but kept for consistency
        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("EMP_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("EMPLOYEE DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 70, 140));
        topPanel.add(title, BorderLayout.CENTER);

        try {
            ImageIcon icon = new ImageIcon("OrientLogo.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(120, 40, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(img));
                topPanel.add(logoLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            // ignore
        }

        add(topPanel, BorderLayout.NORTH);

        // Main content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 100, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Welcome message, name from DB
        JLabel welcomeLabel = new JLabel("Welcome, " + getEmployeeName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeLabel.setForeground(new Color(80, 80, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(welcomeLabel, gbc);

        // Request Vehicle card
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(30, 20, 30, 10);
        JPanel requestCard = createDashboardCard(
                "Request Vehicle",
                "Submit a new vehicle requisition request",
                new Color(0, 120, 215),
                "🚗"
        );
        requestCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                main.showPage("VEHICLE_FORM");
            }
        });
        centerPanel.add(requestCard, gbc);

        // View History card
        gbc.gridx = 1;
        gbc.insets = new Insets(30, 10, 30, 20);
        JPanel historyCard = createDashboardCard(
                "View My History",
                "Check status of your previous requests",
                new Color(70, 150, 70),
                "📋"
        );
        historyCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                main.showPage("EMP_HISTORY");
            }
        });
        centerPanel.add(historyCard, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with logout
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton logoutBtn = createStyledButton("Logout", new Color(200, 80, 80));
        logoutBtn.addActionListener(e -> {
            main.setCurrentEmployeeId(null);
            main.setCurrentRole(null);
            main.setGarageLoggedIn(false);
            main.showPage("LOGIN");
        });
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Get employee name from DB using currentEmployeeId
    private String getEmployeeName() {
        String empId = main.getCurrentEmployeeId();
        if (empId == null || empId.trim().isEmpty()) {
            return "Employee";
        }

        String sql = "SELECT emp_name FROM employees WHERE emp_id = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, empId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("emp_name");
                if (name != null && !name.trim().isEmpty()) {
                    return name.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching employee name: " + e.getMessage());
        }

        return empId; // fallback to ID
    }

    private JPanel createDashboardCard(String title, String description, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setPreferredSize(new Dimension(300, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(24, 24, 24, 24)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(25, 25, 25, 25)
                ));
            }
        });

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setForeground(color);
        card.add(iconLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(60, 60, 60));
        card.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel("<html><body style='width:220px;'>" + description + "</body></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(120, 120, 120));
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
