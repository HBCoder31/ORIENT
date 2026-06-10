// HODApprovalPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.List;

public class HODApprovalPanel extends JPanel {

    private final MainPortal main;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;

    public HODApprovalPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP PANEL WITH LOGO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        // FIX: always go back to HOD dashboard, not employee/admin
        backBtn.addActionListener(e -> main.showPage("HOD_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("HEAD OF DEPARTMENT APPROVAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("0 pending requests", SwingConstants.CENTER);
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

        // ===== TABLE =====
        String[] cols = {"Request ID", "Employee", "From", "To", "Purpose",
                "Status", "HOD", "COO"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }

            @Override
            public Class<?> getColumnClass(int column) { return String.class; }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String hodStatus = (String) getValueAt(row, 6); // HOD column
                    Color bg = Color.WHITE;
                    if ("Approved".equals(hodStatus)) {
                        bg = new Color(220, 255, 220);
                    } else if ("Rejected".equals(hodStatus)) {
                        bg = new Color(255, 220, 220);
                    } else if ("Pending".equals(hodStatus)) {
                        bg = new Color(255, 255, 200);
                        c.setFont(getFont().deriveFont(Font.BOLD));
                    }
                    c.setBackground(bg);
                }
                return c;
            }
        };

        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 25, 20, 25),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(245, 247, 250));
        JLabel infoLabel = new JLabel("Select a request and choose action:");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(infoLabel);
        buttonPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionsPanel.setBackground(new Color(245, 247, 250));

        JButton approveBtn = createActionButton("Approve", new Color(70, 150, 70), "Approved");
        JButton rejectBtn = createActionButton("Reject", new Color(220, 80, 80), "Rejected");
        JButton refreshBtn = createActionButton("Refresh", new Color(100, 130, 200), "refresh");
        JButton viewDetailsBtn = createActionButton("View Details", new Color(255, 140, 0), "details");

        actionsPanel.add(approveBtn);
        actionsPanel.add(rejectBtn);
        actionsPanel.add(refreshBtn);
        actionsPanel.add(viewDetailsBtn);

        buttonPanel.add(actionsPanel, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.SOUTH);

        loadRequestsForHOD();
    }

    private void loadRequestsForHOD() {
        model.setRowCount(0);
        List<VehicleRequest> list = DatabaseOperations.getRequestsForHOD();
        int count = 0;
        for (VehicleRequest r : list) {
            model.addRow(new Object[]{
                    r.getRequestId(),
                    r.getEmpNo(),
                    truncateText(r.getFromLocation(), 15),
                    truncateText(r.getToLocation(), 15),
                    truncateText(r.getPurpose(), 25),
                    r.getStatus(),
                    r.getHodApprovalStatus(),
                    r.getCooApprovalStatus()
            });
            count++;
        }
        statusLabel.setText(count + " pending requests");
    }

    private JButton createActionButton(String text, Color color, String action) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        switch (action) {
            case "Approved":
                btn.addActionListener(e -> handleDecision("Approved"));
                break;
            case "Rejected":
                btn.addActionListener(e -> handleDecision("Rejected"));
                break;
            case "refresh":
                btn.addActionListener(e -> loadRequestsForHOD());
                break;
            case "details":
                btn.addActionListener(e -> showRequestDetails());
                break;
        }
        return btn;
    }

    private void handleDecision(String decision) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reqId = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + decision.toLowerCase() + " request " + reqId + "?",
                "Confirm " + decision,
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String newStatus;
        String hodStatus = null;
        String cooStatus = null;

        if ("Approved".equals(decision)) {
            hodStatus = "Approved";
            String cooCol = (String) model.getValueAt(row, 7);
            if ("Approved".equals(cooCol)) {
                newStatus = "Approved";
            } else if ("Rejected".equals(cooCol)) {
                newStatus = "Rejected by COO";
            } else {
                newStatus = "Waiting for COO Approval";
            }
        } else {
            hodStatus = "Rejected";
            newStatus = "Rejected by HOD";
        }

        boolean ok = DatabaseOperations.updateRequestStatus(
                reqId,
                newStatus,
                hodStatus,
                cooStatus,
                main.getCurrentEmployeeId(),
                "HOD"
        );

        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update request status in database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setValueAt(newStatus, row, 5);
        model.setValueAt(hodStatus, row, 6);

        JOptionPane.showMessageDialog(this,
                "Request " + reqId + " has been " +
                        decision.toLowerCase() + " by HOD.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        loadRequestsForHOD();
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
        String employee = (String) model.getValueAt(modelRow, 1);
        String from = (String) model.getValueAt(modelRow, 2);
        String to = (String) model.getValueAt(modelRow, 3);
        String purpose = (String) model.getValueAt(modelRow, 4);
        String status = (String) model.getValueAt(modelRow, 5);
        String hod = (String) model.getValueAt(modelRow, 6);
        String coo = (String) model.getValueAt(modelRow, 7);

        String details = "Request Details: " + reqId + "\n\n" +
                "Employee: " + employee + "\n" +
                "From: " + from + "\n" +
                "To: " + to + "\n" +
                "Purpose: " + purpose + "\n" +
                "Status: " + status + "\n" +
                "HOD Approval: " + hod + "\n" +
                "COO Approval: " + coo + "\n";

        JOptionPane.showMessageDialog(this,
                details, "Request Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
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
