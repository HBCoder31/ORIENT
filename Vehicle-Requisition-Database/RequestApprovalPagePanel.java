// RequestApprovalPagePanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.List;

public class RequestApprovalPagePanel extends JPanel {

    private final MainPortal main;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    public RequestApprovalPagePanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP PANEL WITH LOGO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("ADMIN_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("ALL VEHICLE REQUESTS - COMPLETE VIEW", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading requests...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(statusLabel, BorderLayout.SOUTH);

        topPanel.add(titlePanel, BorderLayout.CENTER);

        try {
            ImageIcon icon = new ImageIcon("OrientLogo.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(100, 35, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(img));
                topPanel.add(logoLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            // ignore
        }

        add(topPanel, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        // ===== FILTER PANEL =====
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterLabel.setForeground(new Color(0, 70, 140));
        filterPanel.add(filterLabel);

        String[] filters = {"All", "Pending", "Approved", "Rejected", "Assigned", "Completed"};
        JComboBox<String> filterCombo = new JComboBox<>(filters);
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterCombo.addActionListener(e -> applyFilter((String) filterCombo.getSelectedItem()));
        filterPanel.add(filterCombo);

        JButton refreshBtn = createSmallButton("🔄 Refresh", new Color(100, 130, 200));
        refreshBtn.addActionListener(e -> loadAllRequests());
        filterPanel.add(refreshBtn);

        JButton exportBtn = createSmallButton("📊 Export", new Color(70, 150, 70));
        exportBtn.addActionListener(e -> exportToCSV());
        filterPanel.add(exportBtn);

        mainContent.add(filterPanel, BorderLayout.NORTH);

        // ===== ENHANCED TABLE =====
        String[] cols = {
                "Request ID", "Emp ID", "Employee Name",
                "Date", "From", "To", "Purpose",
                "Status", "HOD Status", "COO Status",
                "Vehicle", "Driver"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int column) { return String.class; }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String status = (String) getValueAt(row, 7); // "Status"
                    Color bg = Color.WHITE;

                    if (status != null) {
                        if (status.contains("Approved")) {
                            bg = new Color(220, 255, 220);
                        } else if (status.contains("Rejected")) {
                            bg = new Color(255, 220, 220);
                        } else if (status.contains("Pending") || status.contains("Waiting")) {
                            bg = new Color(255, 255, 200);
                        } else if ("Assigned".equals(status)) {
                            bg = new Color(220, 230, 255);
                        } else if ("Completed".equals(status)) {
                            bg = new Color(230, 230, 230);
                        }
                    }

                    c.setBackground(bg);
                }
                return c;
            }
        };

        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(Color.BLACK);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // ===== BOTTOM PANEL =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // Statistics panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(new Color(245, 247, 250));

        JLabel totalLabel = createStatLabel("0", "Total", new Color(0, 70, 140));
        JLabel pendingLabel = createStatLabel("0", "Pending", new Color(255, 140, 0));
        JLabel approvedLabel = createStatLabel("0", "Approved", new Color(70, 150, 70));
        JLabel assignedLabel = createStatLabel("0", "Assigned", new Color(0, 120, 215));
        JLabel completedLabel = createStatLabel("0", "Completed", new Color(155, 100, 200));

        statsPanel.add(totalLabel);
        statsPanel.add(pendingLabel);
        statsPanel.add(approvedLabel);
        statsPanel.add(assignedLabel);
        statsPanel.add(completedLabel);

        bottomPanel.add(statsPanel, BorderLayout.WEST);

        // Action buttons (only View Details + Garage Panel)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(new Color(245, 247, 250));

        JButton viewDetailsBtn = createSmallButton("👁️ View Details", new Color(255, 140, 0));
        JButton garageBtn = createSmallButton("🚗 Garage Panel", new Color(70, 150, 70));

        viewDetailsBtn.addActionListener(e -> showRequestDetails());
        garageBtn.addActionListener(e -> main.showPage("GARAGE_DASH"));

        actionPanel.add(viewDetailsBtn);
        actionPanel.add(garageBtn);

        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // initial load
        loadAllRequests();
    }

    // === DB-based load ===
    private void loadAllRequests() {
        model.setRowCount(0);

        List<String[]> list = DatabaseOperations.getAllRequests(); // [file:1]

        int total = 0, pending = 0, approved = 0, assigned = 0, completed = 0;

        for (String[] r : list) {
            // r: [requestId, empId, empName, date, from, to, purpose,
            //     status, hodStatus, cooStatus, vehicleName, driverName]
            String requestId   = r[0];
            String empId       = r[1];
            String empName     = r[2];
            String date        = r[3];
            String from        = r[4];
            String to          = r[5];
            String purpose     = r[6];
            String status      = r[7];
            String hodStatus   = r[8];
            String cooStatus   = r[9];
            String vehicleName = r[10];
            String driverName  = r[11];

            model.addRow(new Object[]{
                    requestId,
                    empId,
                    empName,
                    date,
                    truncateText(from, 12),
                    truncateText(to, 12),
                    truncateText(purpose, 20),
                    status,
                    hodStatus,
                    cooStatus,
                    nullToDash(vehicleName),
                    nullToDash(driverName)
            });

            total++;
            if (status.contains("Pending") || status.contains("Waiting")) {
                pending++;
            } else if (status.contains("Approved") && !"Assigned".equals(status)) {
                approved++;
            } else if ("Assigned".equals(status)) {
                assigned++;
            } else if ("Completed".equals(status)) {
                completed++;
            }
        }

        statusLabel.setText(total + " total requests in system");
        updateStatistics(total, pending, approved, assigned, completed);

        if (total == 0) {
            JOptionPane.showMessageDialog(this,
                    "No vehicle requests found in the system.",
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFilter(String filter) {
        if ("All".equals(filter)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i).*" + filter + ".*", 7)); // col 7 = Status
        }
    }

    private void showRequestDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request to view details.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String reqId     = (String) model.getValueAt(modelRow, 0);
        String empId     = (String) model.getValueAt(modelRow, 1);
        String empName   = (String) model.getValueAt(modelRow, 2);
        String date      = (String) model.getValueAt(modelRow, 3);
        String from      = (String) model.getValueAt(modelRow, 4);
        String to        = (String) model.getValueAt(modelRow, 5);
        String purpose   = (String) model.getValueAt(modelRow, 6);
        String status    = (String) model.getValueAt(modelRow, 7);
        String hodStatus = (String) model.getValueAt(modelRow, 8);
        String cooStatus = (String) model.getValueAt(modelRow, 9);
        String vehicle   = (String) model.getValueAt(modelRow, 10);
        String driver    = (String) model.getValueAt(modelRow, 11);

        String details = "<html><div style='font-family: Segoe UI; padding: 10px; width: 400px;'>" +
                "<h3 style='color: #00468c;'>Request Details: " + reqId + "</h3>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr><td style='padding: 5px; font-weight: bold; width: 40%;'>Employee:</td>" +
                "<td style='padding: 5px;'>" + empId + " - " + empName + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Date:</td>" +
                "<td style='padding: 5px;'>" + date + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>From:</td>" +
                "<td style='padding: 5px;'>" + from + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>To:</td>" +
                "<td style='padding: 5px;'>" + to + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Purpose:</td>" +
                "<td style='padding: 5px;'>" + purpose + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Status:</td>" +
                "<td style='padding: 5px;'><b>" + status + "</b></td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>HOD Approval:</td>" +
                "<td style='padding: 5px;'>" + hodStatus + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>COO Approval:</td>" +
                "<td style='padding: 5px;'>" + cooStatus + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Vehicle:</td>" +
                "<td style='padding: 5px;'>" + nullToDash(vehicle) + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Driver:</td>" +
                "<td style='padding: 5px;'>" + nullToDash(driver) + "</td></tr>" +
                "</table></div></html>";

        JOptionPane.showMessageDialog(this, details,
                "Request Details - " + reqId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportToCSV() {
        StringBuilder csv = new StringBuilder();

        for (int i = 0; i < model.getColumnCount(); i++) {
            csv.append(model.getColumnName(i));
            if (i < model.getColumnCount() - 1) csv.append(",");
        }
        csv.append("\n");

        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                String value = String.valueOf(model.getValueAt(row, col));
                if (value.contains(",") || value.contains("\"")) {
                    value = "\"" + value.replace("\"", "\"\"") + "\"";
                }
                csv.append(value);
                if (col < model.getColumnCount() - 1) csv.append(",");
            }
            csv.append("\n");
        }

        JOptionPane.showMessageDialog(this,
                "CSV data ready. Copy this data to a .csv file.\n\n" +
                        "(In full implementation this would write to disk.)",
                "Export Data", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatistics(int total, int pending, int approved, int assigned, int completed) {
        // Currently just logs; you can wire to labels if needed
        System.out.println("Stats - Total: " + total +
                ", Pending: " + pending +
                ", Approved: " + approved +
                ", Assigned: " + assigned +
                ", Completed: " + completed);
    }

    private JLabel createStatLabel(String count, String label, Color color) {
        return new JLabel("<html><div style='text-align: center;'>" +
                "<b style='font-size: 16px; color: " + toHex(color) + ";'>" + count + "</b><br>" +
                "<span style='font-size: 11px; color: #666;'>" + label + "</span></div></html>");
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private JButton createSmallButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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