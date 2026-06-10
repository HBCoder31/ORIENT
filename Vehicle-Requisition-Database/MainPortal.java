// MainPortal.java

import javax.swing.*;
import java.awt.*;

public class MainPortal extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String currentEmployeeId;
    private String currentRole; // Admin, Employee, HOD, COO, Garage
    private JPanel headerPanel;
    private boolean isGarageLoggedIn = false;

    // For passing a selected request from GaragePendingPanel to GarageDashboardPanel
    private String selectedGarageRequestId;

    public MainPortal() {
        setTitle("Vehicle Requisition Portal - CKA Birla Group | ORIENT PAPER");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        // Create enhanced header
        headerPanel = createEnhancedHeader();
        add(headerPanel, BorderLayout.NORTH);

        // CARD PANEL
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(245, 247, 250));
        add(cardPanel, BorderLayout.CENTER);

        // Initialize all panels
        initializePanels();

        showPage("LOGIN");
        setVisible(true);
    }

    private JPanel createEnhancedHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 70, 140));
        header.setPreferredSize(new Dimension(0, 85));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0, 70, 140),
                        0, getHeight(), new Color(0, 90, 160)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 20));

        JLabel portalLabel = new JLabel("VEHICLE REQUISITION PORTAL");
        portalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        portalLabel.setForeground(Color.BLACK); // Note: Set to Color.WHITE if you want it to contrast better against the blue gradient
        titlePanel.add(portalLabel, BorderLayout.WEST);

        JLabel companyLabel = new JLabel("CKA Birla Group | ORIENT PAPER");
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        companyLabel.setForeground(new Color(200, 220, 255));
        companyLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        titlePanel.add(companyLabel, BorderLayout.CENTER);

        header.add(titlePanel, BorderLayout.CENTER);

        // ==========================================
        // UPDATED: Using LogoUtil for consistent global logo
        // ==========================================
        JLabel logoLabel = LogoUtil.createLogoLabel(300, 55);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));
        header.add(logoLabel, BorderLayout.EAST);

        return header;
    }

    private void initializePanels() {
        cardPanel.removeAll();

        // Common login & dashboards
        cardPanel.add(new LoginPagePanel(this), "LOGIN");
        cardPanel.add(new AdminDashboardPanel(this), "ADMIN_DASH");
        cardPanel.add(new EmployeeDashboardPanel(this), "EMP_DASH");
        cardPanel.add(new HODDashboardPanel(this), "HOD_DASH");
        cardPanel.add(new COODashboardPanel(this), "COO_DASH");

        // Garage screens
        cardPanel.add(new GaragePendingPanel(this), "GARAGE_PENDING");
        cardPanel.add(new GarageDashboardPanel(this), "GARAGE_DASH");

        // Vehicle form & employee history depend on currentEmployeeId
        String empId = getCurrentEmployeeId();
        cardPanel.add(new VehicleRequisitionFormPanel(this,
                        empId != null ? "EMP" : "ADMIN", empId),
                "VEHICLE_FORM");

        if (empId != null && !empId.isEmpty()) {
            cardPanel.add(new EmployeeHistoryPagePanel(this, empId), "EMP_HISTORY");
        }

        // Admin tools and approvals
        cardPanel.add(new ViewEmployeesPanel(this), "VIEW_EMP");
        cardPanel.add(new RequestApprovalPagePanel(this), "APPROVAL");
        cardPanel.add(new AdminHistoryPagePanel(this), "ADMIN_HISTORY");
        cardPanel.add(new HODApprovalPanel(this), "HOD_APPROVAL");
        cardPanel.add(new COOApprovalPanel(this), "COO_APPROVAL");
        cardPanel.add(new AuditLogViewerPanel(this), "AUDIT_LOGS");

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public void showPage(String name) {

        // Admin-only logs
        if ("AUDIT_LOGS".equals(name)) {
            if (currentRole == null || !"Admin".equalsIgnoreCase(currentRole)) {
                JOptionPane.showMessageDialog(this,
                        "Only Admin can view audit logs.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Restrict garage login
        if (isGarageLoggedIn) {
            String[] restrictedPages = {
                    "APPROVAL", "HOD_APPROVAL", "COO_APPROVAL", "VIEW_EMP",
                    "ADMIN_HISTORY", "ADMIN_DASH", "EMP_DASH",
                    "VEHICLE_FORM", "EMP_HISTORY", "HOD_DASH", "COO_DASH"
            };
            for (String restrictedPage : restrictedPages) {
                if (name.equals(restrictedPage)) {
                    JOptionPane.showMessageDialog(this,
                            "Garage staff can only access vehicle assignment features.\n" +
                                    "Please use the Garage Dashboard for vehicle assignment.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        // When opening employee-specific pages, ensure employee id is set
        if ("VEHICLE_FORM".equals(name) || "EMP_HISTORY".equals(name)) {
            if (currentEmployeeId == null || currentEmployeeId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Employee ID not available. Please login again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                cardLayout.show(cardPanel, "LOGIN");
                return;
            }
            initializePanels();
        }

        // When showing garage dashboard, refresh selection
        if ("GARAGE_DASH".equals(name)) {
            GarageDashboardPanel gPanel = null;
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof GarageDashboardPanel) {
                    gPanel = (GarageDashboardPanel) comp;
                    break;
                }
            }
            if (gPanel != null) {
                gPanel.refreshFromSelectedId();
            }
        }

        cardLayout.show(cardPanel, name);
    }

    public void setCurrentEmployeeId(String empId) {
        this.currentEmployeeId = empId;
    }

    public String getCurrentEmployeeId() {
        return currentEmployeeId;
    }

    public void setCurrentRole(String role) {
        this.currentRole = role;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setGarageLoggedIn(boolean isGarage) {
        this.isGarageLoggedIn = isGarage;
    }

    // Selected garage request id
    public void setSelectedGarageRequestId(String requestId) {
        this.selectedGarageRequestId = requestId;
    }

    public String getSelectedGarageRequestId() {
        return selectedGarageRequestId;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.createTables();
                DatabaseConnection.testConnection();
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("TextComponent.arc", 5);
                UIManager.put("Panel.background", new Color(245, 247, 250));
                UIManager.put("Button.background", new Color(0, 120, 215));
                UIManager.put("Button.foreground", Color.BLACK);
                UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainPortal();
        });
    }
}