// AdminHistoryPagePanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.List;

public class AdminHistoryPagePanel extends JPanel {

    private final MainPortal main;

    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    public AdminHistoryPagePanel(MainPortal main) {
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

        JLabel title = new JLabel("ADMIN REQUEST HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Viewing all request history", SwingConstants.CENTER);
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
            // Logo not available
        }

        add(topPanel, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ===== STATISTICS PANEL =====
        JPanel statsPanel = createStatisticsPanel();
        mainContent.add(statsPanel, BorderLayout.NORTH);

        // ===== TABLE PANEL =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = {"Request ID", "Employee", "Date", "From", "To",
                "Purpose", "Status", "Vehicle", "Driver", "Completed"};

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
                    String status = (String) getValueAt(row, 6);
                    Color bg = Color.WHITE;
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
                    c.setBackground(bg);
                }
                return c;
            }
        };

        table.setRowHeight(32);
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
        scrollPane.setPreferredSize(new Dimension(800, 350));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(tablePanel, BorderLayout.CENTER);

        // ===== FILTER PANEL =====
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        filterPanel.setBackground(new Color(245, 247, 250));

        JLabel filterLabel = new JLabel("Quick Filters:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterLabel.setForeground(new Color(0, 70, 140));
        filterPanel.add(filterLabel);

        JButton allBtn = createFilterButton("All", new Color(100, 130, 200));
        JButton pendingBtn = createFilterButton("Pending", new Color(255, 140, 0));
        JButton approvedBtn = createFilterButton("Approved", new Color(70, 150, 70));
        JButton assignedBtn = createFilterButton("Assigned", new Color(0, 120, 215));
        JButton completedBtn = createFilterButton("Completed", new Color(155, 100, 200));

        allBtn.addActionListener(e -> { sorter.setRowFilter(null); statusLabel.setText("Showing all requests"); });
        pendingBtn.addActionListener(e -> filterByStatus("Pending"));
        approvedBtn.addActionListener(e -> filterByStatus("Approved"));
        assignedBtn.addActionListener(e -> filterByStatus("Assigned"));
        completedBtn.addActionListener(e -> filterByStatus("Completed"));

        filterPanel.add(allBtn);
        filterPanel.add(pendingBtn);
        filterPanel.add(approvedBtn);
        filterPanel.add(assignedBtn);
        filterPanel.add(completedBtn);

        mainContent.add(filterPanel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        // ===== BOTTOM PANEL =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        exportPanel.setBackground(new Color(245, 247, 250));

        // UPDATED: Green print button for PDF generation
        JButton printBtn = createSmallButton("🖨️ Print / Save PDF", new Color(40, 167, 69));
        printBtn.setForeground(Color.BLACK);

        JButton exportBtn = createSmallButton("💾 Export Data", new Color(70, 150, 70));
        JButton refreshBtn = createSmallButton("🔄 Refresh", new Color(255, 140, 0));

        printBtn.addActionListener(e -> printReport());
        exportBtn.addActionListener(e -> exportData());
        refreshBtn.addActionListener(e -> loadAllRequests());

        exportPanel.add(printBtn);
        exportPanel.add(exportBtn);
        exportPanel.add(refreshBtn);

        bottomPanel.add(exportPanel, BorderLayout.WEST);

        JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        viewPanel.setBackground(new Color(245, 247, 250));

        JButton detailsBtn = createSmallButton("👁️ View Details", new Color(0, 120, 215));
        JButton fullViewBtn = createSmallButton("📋 Full Request View", new Color(155, 100, 200));

        detailsBtn.addActionListener(e -> showRequestDetails());
        fullViewBtn.addActionListener(e -> main.showPage("APPROVAL"));

        viewPanel.add(detailsBtn);
        viewPanel.add(fullViewBtn);

        bottomPanel.add(viewPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Initial load
        loadAllRequests();
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        statsPanel.add(createStatCard("Total", "0", new Color(0, 70, 140)));
        statsPanel.add(createStatCard(" Pending", "0", new Color(255, 140, 0)));
        statsPanel.add(createStatCard(" Approved", "0", new Color(70, 150, 70)));
        statsPanel.add(createStatCard(" Assigned", "0", new Color(0, 120, 215)));
        statsPanel.add(createStatCard(" Completed", "0", new Color(155, 100, 200)));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ==== DB‑DRIVEN LOAD ====
    private void loadAllRequests() {
        model.setRowCount(0);

        List<String[]> list = DatabaseOperations.getAllRequests(); // now from DB
        int total = 0, pending = 0, approved = 0, assigned = 0, completed = 0;

        for (String[] r : list) {
            // getAllRequests returns:
            // [0]=request_id, [1]=emp_id, [2]=emp_name, [3]=request_date,
            // [4]=from, [5]=to, [6]=purpose, [7]=status,
            // [8]=hod_status, [9]=coo_status, [10]=vehicle_name, [11]=driver_name
            String requestId = r[0];
            String empId = r[1];
            String empName = r[2];
            String date = r[3];
            String from = truncateText(r[4], 10);
            String to = truncateText(r[5], 10);
            String purpose = truncateText(r[6], 25);
            String status = r[7];
            String vehicle = nullToDash(r[10]);
            String driver = nullToDash(r[11]);

            // completed flag from status
            String completedFlag = (status != null && status.contains("Completed")) ? "✓ Yes" : "✗ No";

            model.addRow(new Object[]{
                    requestId,
                    empId + " - " + empName,
                    date,
                    from,
                    to,
                    purpose,
                    status,
                    vehicle,
                    driver,
                    completedFlag
            });

            total++;
            if (status != null) {
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
        }

        statusLabel.setText("Showing " + total + " requests");
        updateStatCards(total, pending, approved, assigned, completed);
    }

    private void filterByStatus(String statusFilter) {
        if ("All".equals(statusFilter)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i).*" + statusFilter + ".*", 6)); // Status column
        }
        statusLabel.setText("Filtered by: " + statusFilter);
    }

    private void updateStatCards(int total, int pending, int approved, int assigned, int completed) {
        Component[] components = ((JPanel) ((JPanel) getComponent(1)).getComponent(0)).getComponents();
        if (components.length >= 5) {
            ((JLabel) ((JPanel) components[0]).getComponent(1)).setText(String.valueOf(total));
            ((JLabel) ((JPanel) components[1]).getComponent(1)).setText(String.valueOf(pending));
            ((JLabel) ((JPanel) components[2]).getComponent(1)).setText(String.valueOf(approved));
            ((JLabel) ((JPanel) components[3]).getComponent(1)).setText(String.valueOf(assigned));
            ((JLabel) ((JPanel) components[4]).getComponent(1)).setText(String.valueOf(completed));
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
        String reqId = (String) model.getValueAt(modelRow, 0);

        // Reload details from DB
        String[] req = DatabaseOperations.getVehicleRequestDetails(reqId);
        if (req == null) return;

        String details =
                "Request: " + req[0] + "\n" +
                        "Employee: " + req[1] + " (" + req[2] + ")\n" +
                        "Department: " + req[4] + "\n\n" +
                        "From: " + req[5] + "\n" +
                        "To: " + req[6] + "\n" +
                        "Purpose: " + req[7] + "\n" +
                        "Status: " + req[8] + "\n\n" +
                        "Vehicle: " + nullToDash(req[9]) + " (" + nullToDash(req[10]) + ")\n" +
                        "Driver: " + nullToDash(req[11]) + " (" + nullToDash(req[12]) + ")\n" +
                        "Trip Completed: " + ("1".equals(req[15]) ? "Yes" : "No");

        JOptionPane.showMessageDialog(this, details,
                "Request History - " + reqId,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== UPDATED: ACTUAL PDF PRINTING FUNCTION =====
    private void printReport() {
        try {
            // Setup a header and footer for the PDF document
            java.text.MessageFormat header = new java.text.MessageFormat("Admin Vehicle History Report");
            java.text.MessageFormat footer = new java.text.MessageFormat("Page - {0} | CKA Birla Group - Orient Paper");

            // FIT_WIDTH ensures all columns shrink to fit on one PDF page horizontally
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, header, footer);

            if (complete) {
                JOptionPane.showMessageDialog(this,
                        "Report successfully processed.",
                        "Print Status",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException pe) {
            JOptionPane.showMessageDialog(this,
                    "Failed to generate PDF/Print: " + pe.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportData() {
        JOptionPane.showMessageDialog(this,
                "Export feature would save data to Excel/CSV in full implementation.",
                "Export Data",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createFilterButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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

    private String truncateText(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}