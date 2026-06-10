// AdminDashboardPanel.java

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private final MainPortal main;

    public AdminDashboardPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Top panel with logo
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("ADMIN_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("ADMIN CONTROL PANEL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 70, 140));
        topPanel.add(title, BorderLayout.CENTER);

        // Logo
        try {
            ImageIcon icon = new ImageIcon("OrientLogo.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(120, 40, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(img));
                topPanel.add(logoLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            // Logo not available
        }

        add(topPanel, BorderLayout.NORTH);

        // Main content - Grid of cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 60, 100));

        // Card 1: View Employees
        centerPanel.add(createAdminCard("View Employees", "Manage employee database",
                new Color(0, 120, 215), "VIEW_EMP"));

        // Card 2: View Requests
        centerPanel.add(createAdminCard("Vehicle Requests", "Approve/Reject vehicle requests",
                new Color(70, 150, 70), "APPROVAL"));

        // Card 3: Request Vehicle
        centerPanel.add(createAdminCard("Request Vehicle", "Submit vehicle requisition",
                new Color(255, 140, 0), "VEHICLE_FORM"));

        // Card 4: Admin History
        centerPanel.add(createAdminCard(" Admin History", "View all request history",
                new Color(155, 100, 200), "ADMIN_HISTORY"));

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        // Quick actions panel
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        quickActions.setBackground(new Color(245, 247, 250));

        JButton hodBtn = createQuickActionButton("HOD Approval", "HOD_APPROVAL", new Color(40, 110, 180));
        JButton cooBtn = createQuickActionButton("COO Approval", "COO_APPROVAL", new Color(60, 130, 200));

        // NEW: Audit Logs quick action, same style helper
        JButton auditBtn = createQuickActionButton("Audit Logs", "AUDIT_LOGS", new Color(150, 150, 200));

        quickActions.add(hodBtn);
        quickActions.add(cooBtn);
        quickActions.add(auditBtn); // added, existing buttons unchanged

        bottomPanel.add(quickActions, BorderLayout.CENTER);

        // Logout button
        JButton logoutBtn = createStyledButton("Logout", new Color(200, 80, 80));
        logoutBtn.addActionListener(e -> main.showPage("LOGIN"));
        bottomPanel.add(logoutBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createAdminCard(String title, String description, Color color, String page) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(29, 29, 29, 29)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                main.showPage(page);
            }
        });

        // Icon and title
        String icon = title.substring(0, title.indexOf(' '));
        String text = title.substring(title.indexOf(' ') + 1);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        iconLabel.setForeground(color);
        card.add(iconLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(60, 60, 60));
        card.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        card.add(descLabel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createQuickActionButton(String text, String page, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> main.showPage(page));
        return btn;
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
