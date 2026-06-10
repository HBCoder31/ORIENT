// EmployeeHistoryPagePanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class EmployeeHistoryPagePanel extends JPanel {

    private final MainPortal main;
    private final String empNo;

    private DefaultTableModel model;
    private JTable table;

    public EmployeeHistoryPagePanel(MainPortal main, String empNo) {
        this.main = main;
        this.empNo = empNo;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Top panel with logo
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("EMP_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("MY VEHICLE REQUEST HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        topPanel.add(title, BorderLayout.CENTER);

        // Logo in top right
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

        // Table with enhanced styling
        String[] cols = {
                "Request ID", "Date", "From", "To", "Purpose",
                "Status", "HOD", "COO", "Vehicle", "Number",
                "Driver", "Contact", "Completed"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String status = (String) getValueAt(row, 5);
                    Color bg = Color.WHITE;
                    if (status.contains("Approved")) {
                        bg = new Color(220, 255, 220);
                    } else if (status.contains("Rejected")) {
                        bg = new Color(255, 220, 220);
                    } else if (status.contains("Pending") || status.contains("Waiting")) {
                        bg = new Color(255, 255, 200);
                    } else if (status.contains("Assigned")) {
                        bg = new Color(220, 230, 255);
                    } else if (status.contains("Completed")) {
                        bg = new Color(230, 230, 230);
                    }
                    c.setBackground(bg);
                }
                return c;
            }
        };

        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 70, 140));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        JButton refreshBtn = createStyledButton("🔄 Refresh", new Color(0, 120, 215));
        refreshBtn.addActionListener(e -> loadHistoryForEmployee());
        bottomPanel.add(refreshBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        loadHistoryForEmployee();
    }

    private void loadHistoryForEmployee() {
        model.setRowCount(0);
        if (empNo == null || empNo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID is not available. Please re‑login.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String[]> list = DatabaseOperations.getEmployeeRequests(empNo);
        if (list == null || list.isEmpty()) {
            // Optional: show message if you want
            // JOptionPane.showMessageDialog(this,
            //         "No vehicle request history found.",
            //         "Information",
            //         JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (String[] r : list) {
            // r indices: [0..13] per getEmployeeRequests
            String requestId = r[0];
            String date = r[1] != null ? r[1] : "-";
            String from = nullToDash(r[2]);
            String to = nullToDash(r[3]);
            String purpose = truncateText(r[4], 30);
            String status = nullToDash(r[5]);
            String hod = nullToDash(r[6]);
            String coo = nullToDash(r[7]);
            String vehicle = nullToDash(r[8]);
            String vehicleNumber = nullToDash(r[9]);
            String driver = nullToDash(r[10]);
            String driverContact = nullToDash(r[11]);
            boolean tripCompleted = "1".equals(r[12]);
            String completed = tripCompleted ? "✓ Yes" : "✗ No";
            // requester contact is r[13] if you want to display elsewhere

            model.addRow(new Object[]{
                    requestId,
                    date,
                    from,
                    to,
                    purpose,
                    status,
                    hod,
                    coo,
                    vehicle,
                    vehicleNumber,
                    driver,
                    driverContact,
                    completed
            });
        }

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
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
