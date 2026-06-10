// HODDashboardPanel.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HODDashboardPanel extends JPanel {

    private final MainPortal main;

    public HODDashboardPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP BLUE HEADER =====
        JPanel headerStrip = new JPanel(new BorderLayout());
        headerStrip.setBackground(new Color(0, 70, 140));
        headerStrip.setPreferredSize(new Dimension(0, 80));

        JButton backBtn = new JButton("← Back");
        backBtn.setBackground(new Color(0, 70, 140));
        backBtn.setForeground(Color.BLACK);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> main.showPage("LOGIN")); // or ADMIN_DASH if needed
        headerStrip.add(backBtn, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel portalLabel = new JLabel("VEHICLE REQUISITION PORTAL");
        portalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        portalLabel.setForeground(Color.BLACK);
        JLabel companyLabel = new JLabel("CKA Birla Group | ORIENT PAPER");
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        companyLabel.setForeground(new Color(200, 220, 255));
        textPanel.add(portalLabel);
        textPanel.add(companyLabel);
        headerStrip.add(textPanel, BorderLayout.CENTER);

        add(headerStrip, BorderLayout.NORTH);

        // ===== CENTER MAIN AREA =====
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(245, 247, 250));
        mainArea.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("HOD CONTROL PANEL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(0, 70, 140));
        mainArea.add(title, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardsPanel.setBackground(new Color(245, 247, 250));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        // Card 1: Request Vehicle
        JPanel requestCard = createCard(
                "Request",
                "Request Vehicle",
                "Submit vehicle requisition as HOD",
                new Color(255, 140, 0)
        );
        JButton requestBtn = (JButton) requestCard.getClientProperty("actionButton");
        requestBtn.addActionListener(e -> {
            main.setCurrentRole("HOD");
            main.showPage("VEHICLE_FORM");
        });

        // Card 2: My History
        JPanel historyCard = createCard(
                "My",
                "My History",
                "View your vehicle request history",
                new Color(128, 0, 255)
        );
        JButton historyBtn = (JButton) historyCard.getClientProperty("actionButton");
        historyBtn.addActionListener(e -> main.showPage("EMP_HISTORY"));

        // Card 3: Approval Requests
        JPanel approvalCard = createCard(
                "Approval",
                "Approval Requests",
                "Approve/Reject department requests",
                new Color(0, 160, 0)
        );
        JButton approvalBtn = (JButton) approvalCard.getClientProperty("actionButton");
        approvalBtn.addActionListener(e -> main.showPage("HOD_APPROVAL"));

        cardsPanel.add(requestCard);
        cardsPanel.add(historyCard);
        cardsPanel.add(approvalCard);

        mainArea.add(cardsPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // ===== BOTTOM RIGHT LOGOUT =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 25));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(180, 80, 80), 2));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            main.setCurrentEmployeeId(null);
            main.setCurrentRole(null);
            main.setGarageLoggedIn(false);
            main.showPage("LOGIN");
        });

        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoutWrap.setOpaque(false);
        logoutWrap.add(logoutBtn);
        bottomPanel.add(logoutWrap, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCard(String bigTitle,
                              String midTitle,
                              String description,
                              Color accentColor) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JLabel big = new JLabel(bigTitle);
        big.setFont(new Font("Segoe UI", Font.BOLD, 28));
        big.setForeground(accentColor);
        card.add(big, BorderLayout.NORTH);

        JLabel mid = new JLabel(midTitle);
        mid.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mid.setForeground(Color.BLACK);

        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setOpaque(false);
        midPanel.add(mid, BorderLayout.NORTH);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(new Color(120, 120, 120));

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        descPanel.add(desc, BorderLayout.SOUTH);

        midPanel.add(descPanel, BorderLayout.SOUTH);
        card.add(midPanel, BorderLayout.CENTER);

        // Hidden button to attach action logic
        JButton actionBtn = new JButton();
        actionBtn.setVisible(false);
        card.putClientProperty("actionButton", actionBtn);

        // Make whole card clickable
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (ActionListener al : actionBtn.getActionListeners()) {
                    al.actionPerformed(
                            new ActionEvent(card, ActionEvent.ACTION_PERFORMED, "")
                    );
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.setBackground(new Color(250, 250, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(Cursor.getDefaultCursor());
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }
}
