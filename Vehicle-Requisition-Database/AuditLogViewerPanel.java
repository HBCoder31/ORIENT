// AuditLogViewerPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AuditLogViewerPanel extends JPanel {

    private final MainPortal main;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;

    public AuditLogViewerPanel(MainPortal main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // TOP
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("ADMIN_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("AUDIT LOGS - WHO DID WHAT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading logs...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(statusLabel, BorderLayout.SOUTH);

        topPanel.add(titlePanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // TABLE
        String[] cols = {
                "Time", "User", "Role", "Action",
                "Entity Type", "Entity ID", "Description"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String action = (String) getValueAt(row, 3);
                    Color bg = Color.WHITE;
                    if (action != null) {
                        if (action.contains("LOGIN")) {
                            bg = new Color(230, 240, 255);
                        } else if (action.contains("CREATE") || action.contains("ASSIGN")) {
                            bg = new Color(230, 255, 230);
                        } else if (action.contains("UPDATE") || action.contains("COMPLETE")) {
                            bg = new Color(255, 245, 230);
                        }
                    }
                    c.setBackground(bg);
                }
                return c;
            }
        };
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 25, 10, 25),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));
        add(scroll, BorderLayout.CENTER);

        // BOTTOM
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(new Color(245, 247, 250));

        JButton refreshBtn = createSmallButton("Refresh", new Color(100, 130, 200));
        refreshBtn.addActionListener(e -> loadLogs());
        bottom.add(refreshBtn);

        JButton closeBtn = createSmallButton("Clear Filter", new Color(155, 100, 200));
        closeBtn.addActionListener(e -> statusLabel.setText("Showing all logs"));
        bottom.add(closeBtn);

        add(bottom, BorderLayout.SOUTH);

        // Initial load
        loadLogs();
    }

    private void loadLogs() {
        model.setRowCount(0);
        int count = 0;

        String sql = "SELECT timestamp, actor_empid, actor_role, action_type, " +
                "entity_type, entity_id, description " +
                "FROM audit_log ORDER BY timestamp DESC LIMIT 500";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getTimestamp("timestamp").toString());
                row.add(rs.getString("actor_empid"));
                row.add(rs.getString("actor_role"));
                row.add(rs.getString("action_type"));
                row.add(rs.getString("entity_type"));
                row.add(rs.getString("entity_id"));
                row.add(rs.getString("description"));
                model.addRow(row);
                count++;
            }
            statusLabel.setText("Showing " + count + " latest log entries");
        } catch (Exception e) {
            statusLabel.setText("Error loading logs: " + e.getMessage());
        }
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
