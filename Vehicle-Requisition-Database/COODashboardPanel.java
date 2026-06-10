// COODashboardPanel.java

import javax.swing.*;
import java.awt.*;

public class COODashboardPanel extends JPanel {

    private final MainPortal main;

    public COODashboardPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("LOGIN"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("COO CONTROL PANEL", SwingConstants.CENTER);
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
        } catch (Exception ex) {
            // ignore
        }

        add(topPanel, BorderLayout.NORTH);

        // Center grid
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 30, 30));
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 60, 80));

        centerPanel.add(createCard("Request Vehicle",
                "Submit vehicle requisition as COO",
                new Color(255, 140, 0),
                "VEHICLE_FORM"));
        centerPanel.add(createCard("My History",
                "View your vehicle request history",
                new Color(155, 100, 200),
                "EMP_HISTORY"));
        centerPanel.add(createCard("Approval Requests",
                "Approve/Reject all department requests",
                new Color(70, 150, 70),
                "COO_APPROVAL"));

        add(centerPanel, BorderLayout.CENTER);

        // Bottom logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JButton logoutBtn = createStyledButton("Logout", new Color(200, 80, 80));
        logoutBtn.addActionListener(e -> {
            main.setCurrentEmployeeId(null);
            main.setCurrentRole(null);
            main.showPage("LOGIN");
        });
        bottomPanel.add(logoutBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCard(String title, String desc, Color color, String page) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(29, 29, 29, 29)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                main.showPage(page);
            }
        });

        JLabel iconLabel = new JLabel(title.split(" ")[0]);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        iconLabel.setForeground(color);
        card.add(iconLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(60, 60, 60));
        card.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
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

