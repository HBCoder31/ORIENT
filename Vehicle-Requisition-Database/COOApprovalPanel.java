// COOApprovalPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class COOApprovalPanel extends JPanel {

    private final MainPortal main;
    private JTable table;
    private DefaultTableModel model;

    public COOApprovalPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP PANEL WITH LOGO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        // Back to COO dashboard
        backBtn.addActionListener(e -> main.showPage("COO_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("COO APPROVAL PANEL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        topPanel.add(title, BorderLayout.CENTER);

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
        String[] cols = {"Request ID", "Emp No", "From", "To", "Purpose",
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
                    String cooStatus = (String) getValueAt(row, 7); // COO column
                    Color bg = Color.WHITE;
                    if ("Approved".equals(cooStatus)) {
                        bg = new Color(220, 255, 220);
                    } else if ("Rejected".equals(cooStatus)) {
                        bg = new Color(255, 220, 220);
                    } else if ("Pending".equals(cooStatus)) {
                        bg = new Color(255, 255, 200);
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));

        JButton approveBtn = createActionButton("Approve", new Color(70, 150, 70), "Approved");
        JButton rejectBtn = createActionButton("Reject", new Color(220, 80, 80), "Rejected");
        JButton refreshBtn = createActionButton("Refresh", new Color(100, 130, 200), "refresh");
        JButton viewDetailsBtn = createActionButton("View Details", new Color(255, 140, 0), "details");

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewDetailsBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        loadRequestsForCOO();
    }

    private void loadRequestsForCOO() {
        model.setRowCount(0);
        List<VehicleRequest> list = DatabaseOperations.getRequestsForCOO();
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
                btn.addActionListener(e -> loadRequestsForCOO());
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

        String hodCol = (String) model.getValueAt(row, 6);

        if ("Approved".equals(decision)) {
            cooStatus = "Approved";
            if ("Approved".equals(hodCol)) {
                newStatus = "Approved";
            } else if ("Rejected".equals(hodCol)) {
                newStatus = "Rejected by HOD";
            } else if ("Pending".equals(hodCol)) {
                newStatus = "Waiting for HOD Approval";
            } else {
                newStatus = "Pending";
            }
        } else {
            cooStatus = "Rejected";
            if ("Approved".equals(hodCol)) {
                newStatus = "Rejected by COO";
            } else if ("Rejected".equals(hodCol)) {
                newStatus = "Rejected";
            } else if ("Pending".equals(hodCol)) {
                newStatus = "Waiting for HOD Approval";
            } else {
                newStatus = "Pending";
            }
        }

        boolean ok = DatabaseOperations.updateRequestStatus(
                reqId,
                newStatus,
                hodStatus,
                cooStatus,
                main.getCurrentEmployeeId(),
                "COO"
        );

        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update request in database.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setValueAt(newStatus, row, 5);
        model.setValueAt(cooStatus, row, 7);

        JOptionPane.showMessageDialog(this,
                "Request " + reqId + " has been " + decision.toLowerCase() + " by COO.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
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

        String reqId = (String) model.getValueAt(row, 0);

        String[] details = DatabaseOperations.getVehicleRequestDetails(reqId);
        if (details == null) {
            JOptionPane.showMessageDialog(this,
                    "Request not found in database.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String detailsText =
                "Request Details\n\n" +
                "Request ID: " + details[0] + "\n" +
                "Employee ID: " + details[1] + "\n" +
                "Employee Name: " + details[2] + "\n" +
                "Designation: " + details[3] + "\n" +
                "Department: " + details[4] + "\n" +
                "From: " + details[5] + "\n" +
                "To: " + details[6] + "\n" +
                "Purpose: " + details[7] + "\n" +
                "Status: " + details[8] + "\n" +
                "Vehicle: " + (details[9] != null ? details[9] : "-") + "\n" +
                "Vehicle No: " + (details[10] != null ? details[10] : "-") + "\n" +
                "Driver: " + (details[11] != null ? details[11] : "-") + "\n" +
                "Driver Contact: " + (details[12] != null ? details[12] : "-") + "\n" +
                "Pickup: " + (details[13] != null ? details[13] : "-") + "\n" +
                "Dropoff: " + (details[14] != null ? details[14] : "-") + "\n" +
                "Trip Completed: " + ("1".equals(String.valueOf(details[15])) ? "Yes" : "No") + "\n" +
                "Requester Contact: " + (details[16] != null ? details[16] : "-");

        JOptionPane.showMessageDialog(this,
                detailsText,
                "Request Details",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
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
}
