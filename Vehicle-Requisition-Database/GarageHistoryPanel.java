// GarageHistoryPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class GarageHistoryPanel extends JPanel {

    private final MainPortal main;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public GarageHistoryPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP PANEL WITH LOGO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("GARAGE_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("GARAGE VEHICLE ASSIGNMENT HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading assignment history...", SwingConstants.CENTER);
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
            // ignore logo errors
        }

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE PANEL =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = {
                "Assign Time", "Request ID", "Employee",
                "From", "To", "Purpose",
                "Vehicle", "Number", "Driver", "Driver Contact",
                "Pickup", "Drop-off", "Completed", "Assigned By"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String completed = (String) getValueAt(row, 12);
                    Color bg = Color.WHITE;
                    if ("Yes".equalsIgnoreCase(completed)) {
                        bg = new Color(230, 230, 230);  // Completed – grey
                    } else {
                        bg = new Color(220, 230, 255);  // Assigned – bluish
                    }
                    c.setBackground(bg);
                }
                return c;
            }
        };

        table.setRowHeight(28);
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1200, 350));
        tablePanel.add(scroll, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // ===== BOTTOM BUTTONS =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(new Color(245, 247, 250));

        JButton refreshBtn = createSmallButton("Refresh", new Color(100, 130, 200));
        refreshBtn.addActionListener(e -> loadHistory());
        bottom.add(refreshBtn);

        JButton showAssignedBtn = createSmallButton("Show Assigned Only", new Color(155, 100, 200));
        showAssignedBtn.addActionListener(e -> applyCompletedFilter(false));
        bottom.add(showAssignedBtn);

        JButton showCompletedBtn = createSmallButton("Show Completed Only", new Color(70, 150, 70));
        showCompletedBtn.addActionListener(e -> applyCompletedFilter(true));
        bottom.add(showCompletedBtn);

        JButton showAllBtn = createSmallButton("Show All", new Color(200, 140, 0));
        showAllBtn.addActionListener(e -> {
            sorter.setRowFilter(null);
            statusLabel.setText("Showing all assignments");
        });
        bottom.add(showAllBtn);

        add(bottom, BorderLayout.SOUTH);

        // Initial load
        loadHistory();
    }

    private void loadHistory() {
        model.setRowCount(0);
        int count = 0;

        // Join vehicle_assignments with vehicle_requests and employees
        String sql =
                "SELECT va.assignment_date, va.completion_date, va.request_id, " +
                "       va.vehicle_name, va.vehicle_number, va.driver_name, va.driver_contact, " +
                "       va.pickup_datetime, va.dropoff_datetime, va.trip_completed, va.assigned_by, " +
                "       vr.from_location, vr.to_location, vr.purpose, " +
                "       e.emp_name " +
                "FROM vehicle_assignments va " +
                "JOIN vehicle_requests vr ON va.request_id = vr.request_id " +
                "JOIN employees e ON vr.emp_id = e.emp_id " +
                "ORDER BY va.assignment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Timestamp assignTs = rs.getTimestamp("assignment_date");
                Timestamp pickupTs = rs.getTimestamp("pickup_datetime");
                Timestamp dropTs = rs.getTimestamp("dropoff_datetime");
                boolean completed = rs.getBoolean("trip_completed");

                String assignTime = assignTs != null ? dateTimeFormat.format(assignTs) : "-";
                String pickup = pickupTs != null ? dateTimeFormat.format(pickupTs) : "-";
                String dropoff = dropTs != null ? dateTimeFormat.format(dropTs) : "-";

                model.addRow(new Object[]{
                        assignTime,
                        rs.getString("request_id"),
                        rs.getString("emp_name"),
                        rs.getString("from_location"),
                        rs.getString("to_location"),
                        rs.getString("purpose"),
                        rs.getString("vehicle_name"),
                        rs.getString("vehicle_number"),
                        rs.getString("driver_name"),
                        rs.getString("driver_contact"),
                        pickup,
                        dropoff,
                        completed ? "Yes" : "No",
                        rs.getString("assigned_by")
                });
                count++;
            }

            statusLabel.setText("Showing " + count + " assignment record(s)");

        } catch (Exception e) {
            statusLabel.setText("Error loading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyCompletedFilter(boolean completedOnly) {
        sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String val = (String) entry.getValue(12); // "Completed" column
                boolean isCompleted = "Yes".equalsIgnoreCase(val);
                return completedOnly == isCompleted;
            }
        });
        statusLabel.setText(completedOnly
                ? "Showing completed trips only"
                : "Showing assigned (not completed) trips only");
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
}
