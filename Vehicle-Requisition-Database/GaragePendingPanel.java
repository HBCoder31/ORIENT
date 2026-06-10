// GaragePendingPanel.java
// Simple list of pending requests that need vehicle assignment

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GaragePendingPanel extends JPanel {

    private final MainPortal main;
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dbDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public GaragePendingPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP BAR =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("GARAGE_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("GARAGE PENDING REQUESTS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading pending requests...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(statusLabel, BorderLayout.SOUTH);

        topPanel.add(titlePanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 20, 25));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = {
                "Req ID", "Employee", "From", "To", "Purpose", "Status",
                "Pickup Date", "Pickup Time"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }

            @Override
            public Class<?> getColumnClass(int column) { return String.class; }
        };

        requestTable = new JTable(tableModel);
        requestTable.setRowHeight(28);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setShowGrid(true);
        requestTable.setGridColor(new Color(230, 230, 230));
        requestTable.setSelectionBackground(new Color(200, 220, 255));
        requestTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setPreferredSize(new Dimension(900, 350));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ===== BOTTOM BUTTONS =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        JButton openAssignBtn = createStyledButton("Open Assignment Dashboard", new Color(70, 150, 70));
        openAssignBtn.addActionListener(e -> {
            int row = requestTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a request first.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = requestTable.convertRowIndexToModel(row);
            String requestId = (String) tableModel.getValueAt(modelRow, 0);

            // store selected request id in MainPortal
            main.setSelectedGarageRequestId(requestId);

            // open the existing assignment dashboard card
            main.showPage("GARAGE_DASH");
        });
        bottomPanel.add(openAssignBtn);

        JButton refreshBtn = createStyledButton("Refresh", new Color(100, 130, 200));
        refreshBtn.addActionListener(e -> loadPendingRequests());
        bottomPanel.add(refreshBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // initial load
        loadPendingRequests();
    }

    private void loadPendingRequests() {
        tableModel.setRowCount(0);

        List<String[]> list = DatabaseOperations.getGarageRequests(); // [0..15]
        int count = 0;

        for (String[] r : list) {
            String status = r[8];

            // show items that are still waiting for garage (you can tweak as per rules)
            if (!"Approved by HOD".equals(status) &&
                    !"Approved (HOD & COO)".equals(status) &&
                    !"Waiting for COO Approval".equals(status)) {
                continue;
            }

            String pickupDate = "-";
            String pickupTime = "-";
            try {
                if (r[13] != null && !r[13].isEmpty()) {
                    Date dt = dbDateTimeFormat.parse(r[13]);
                    pickupDate = displayDateFormat.format(dt);
                    pickupTime = displayTimeFormat.format(dt);
                }
            } catch (ParseException e) {
                // keep "-"
            }

            tableModel.addRow(new Object[]{
                    r[0],                              // request_id
                    r[1] + " - " + truncate(r[2], 18), // emp_id + name
                    truncate(r[5], 12),                // from_location
                    truncate(r[6], 12),                // to_location
                    truncate(r[7], 25),                // purpose
                    status,
                    pickupDate,
                    pickupTime
            });
            count++;
        }

        statusLabel.setText(count + " pending request(s) for garage");
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String truncate(String s, int max) {
        if (s == null) return "-";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
