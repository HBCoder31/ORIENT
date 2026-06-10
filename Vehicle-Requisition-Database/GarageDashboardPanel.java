// GarageDashboardPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class GarageDashboardPanel extends JPanel {

    private final MainPortal main;
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // Input fields for vehicle assignment
    private JTextField vehicleNameField;
    private JTextField vehicleNumberField;
    private JTextField driverNameField;
    private JTextField driverContactField;
    private JCheckBox vehicleAvailableCheck;
    private JCheckBox tripCompletedCheck;

    // Date and time fields for pickup/drop-off
    private JTextField pickupDateField;
    private JTextField pickupTimeField;
    private JTextField dropoffDateField;
    private JTextField dropoffTimeField;

    // Intender details fields (read-only)
    private JTextField intenderNameField;
    private JTextField intenderDeptField;
    private JTextField intenderDesignationField;
    private JTextField intenderRequestContactField;

    // Database date formats
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dbDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat inputDateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public GarageDashboardPanel(MainPortal main) {
        this.main = main;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== TOP PANEL WITH LOGO =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createHeaderButton("← Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("GARAGE_PENDING"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("GARAGE VEHICLE ASSIGNMENT DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready to assign vehicles", SwingConstants.CENTER);
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

        // ===== MAIN CONTENT PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ===== TABLE PANEL =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = {"Req ID", "Employee", "From", "To", "Purpose", "Status",
                "Pickup Date", "Pickup Time", "Drop-off Date", "Drop-off Time",
                "Vehicle", "Number", "Driver", "Contact", "Completed"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int column) { return String.class; }
        };

        requestTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String status = (String) getValueAt(row, 5);
                    Color bg = Color.WHITE;
                    if ("Assigned".equals(status)) {
                        bg = new Color(220, 230, 255);
                    } else if ("Completed".equals(status)) {
                        bg = new Color(230, 230, 230);
                    } else if (status != null && status.contains("Approved")) {
                        bg = new Color(220, 255, 220);
                    } else if (status != null && status.contains("Pending")) {
                        bg = new Color(255, 255, 200);
                    }
                    c.setBackground(bg);
                }
                return c;
            }
        };

        requestTable.setRowHeight(30);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setShowGrid(true);
        requestTable.setGridColor(new Color(230, 230, 230));
        requestTable.setSelectionBackground(new Color(200, 220, 255));
        requestTable.setSelectionForeground(Color.BLACK);

        // Column widths
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        requestTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        requestTable.getColumnModel().getColumn(9).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(10).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(11).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(12).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(13).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(14).setPreferredWidth(70);

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setPreferredSize(new Dimension(1200, 250));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // ===== BOTTOM PANEL WITH TWO SECTIONS =====
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        bottomPanel.setBackground(new Color(245, 247, 250));

        // INTENDER DETAILS PANEL
        JPanel intenderPanel = new JPanel(new GridBagLayout());
        intenderPanel.setBackground(Color.WHITE);
        intenderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel intenderTitle = new JLabel("Intender Details");
        intenderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        intenderTitle.setForeground(new Color(0, 70, 140));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        intenderPanel.add(intenderTitle, gbc);

        // Name
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel intenderNameLbl = new JLabel("Intender Name:");
        intenderNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        intenderPanel.add(intenderNameLbl, gbc);

        gbc.gridx = 1;
        intenderNameField = createReadOnlyTextField();
        intenderPanel.add(intenderNameField, gbc);

        // Department
        gbc.gridx = 2;
        JLabel intenderDeptLbl = new JLabel("Department:");
        intenderDeptLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        intenderPanel.add(intenderDeptLbl, gbc);

        gbc.gridx = 3;
        intenderDeptField = createReadOnlyTextField();
        intenderPanel.add(intenderDeptField, gbc);

        // Designation
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel intenderDesigLbl = new JLabel("Designation:");
        intenderDesigLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        intenderPanel.add(intenderDesigLbl, gbc);

        gbc.gridx = 1;
        intenderDesignationField = createReadOnlyTextField();
        intenderPanel.add(intenderDesignationField, gbc);

        // Contact
        gbc.gridx = 2;
        JLabel intenderContactLbl = new JLabel("Contact No:");
        intenderContactLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        intenderPanel.add(intenderContactLbl, gbc);

        gbc.gridx = 3;
        intenderRequestContactField = createReadOnlyTextField();
        intenderPanel.add(intenderRequestContactField, gbc);

        bottomPanel.add(intenderPanel);

        // VEHICLE & TIMING PANEL
        JPanel assignmentPanel = new JPanel(new GridBagLayout());
        assignmentPanel.setBackground(Color.WHITE);
        assignmentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel assignTitle = new JLabel("Vehicle & Timing Details");
        assignTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        assignTitle.setForeground(new Color(0, 70, 140));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
        assignmentPanel.add(assignTitle, gbc);

        // Row 1: Vehicle
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel vehicleNameLbl = new JLabel("Vehicle Name:");
        vehicleNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(vehicleNameLbl, gbc);

        gbc.gridx = 1;
        vehicleNameField = createStyledTextField();
        assignmentPanel.add(vehicleNameField, gbc);

        gbc.gridx = 2;
        JLabel vehicleNumberLbl = new JLabel("Vehicle Number:");
        vehicleNumberLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(vehicleNumberLbl, gbc);

        gbc.gridx = 3;
        vehicleNumberField = createStyledTextField();
        assignmentPanel.add(vehicleNumberField, gbc);

        gbc.gridx = 4;
        JLabel pickupDateLbl = new JLabel("Pickup Date:");
        pickupDateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(pickupDateLbl, gbc);

        gbc.gridx = 5;
        pickupDateField = createStyledTextField();
        pickupDateField.setToolTipText("DD-MM-YYYY");
        assignmentPanel.add(pickupDateField, gbc);

        // Row 2: Driver
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel driverNameLbl = new JLabel("Driver Name:");
        driverNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(driverNameLbl, gbc);

        gbc.gridx = 1;
        driverNameField = createStyledTextField();
        assignmentPanel.add(driverNameField, gbc);

        gbc.gridx = 2;
        JLabel driverContactLbl = new JLabel("Driver Contact:");
        driverContactLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(driverContactLbl, gbc);

        gbc.gridx = 3;
        driverContactField = createStyledTextField();
        assignmentPanel.add(driverContactField, gbc);

        gbc.gridx = 4;
        JLabel pickupTimeLbl = new JLabel("Pickup Time:");
        pickupTimeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(pickupTimeLbl, gbc);

        gbc.gridx = 5;
        pickupTimeField = createStyledTextField();
        pickupTimeField.setToolTipText("HH:MM (24-hour format)");
        assignmentPanel.add(pickupTimeField, gbc);

        // Row 3: flags + drop-off date
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        vehicleAvailableCheck = new JCheckBox("Vehicle Available");
        vehicleAvailableCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
        vehicleAvailableCheck.setBackground(Color.WHITE);
        vehicleAvailableCheck.setSelected(true);
        assignmentPanel.add(vehicleAvailableCheck, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        tripCompletedCheck = new JCheckBox("Trip Completed");
        tripCompletedCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tripCompletedCheck.setBackground(Color.WHITE);
        assignmentPanel.add(tripCompletedCheck, gbc);

        gbc.gridx = 4; gbc.gridwidth = 1;
        JLabel dropoffDateLbl = new JLabel("Drop-off Date:");
        dropoffDateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(dropoffDateLbl, gbc);

        gbc.gridx = 5;
        dropoffDateField = createStyledTextField();
        dropoffDateField.setToolTipText("DD-MM-YYYY");
        assignmentPanel.add(dropoffDateField, gbc);

        // Row 4: drop-off time
        gbc.gridx = 4; gbc.gridy = 4;
        JLabel dropoffTimeLbl = new JLabel("Drop-off Time:");
        dropoffTimeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignmentPanel.add(dropoffTimeLbl, gbc);

        gbc.gridx = 5;
        dropoffTimeField = createStyledTextField();
        dropoffTimeField.setToolTipText("HH:MM (24-hour format)");
        assignmentPanel.add(dropoffTimeField, gbc);

        bottomPanel.add(assignmentPanel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));

        JButton assignBtn = createActionButton("Assign Vehicle", new Color(70, 150, 70), "assign");
        JButton updateBtn = createActionButton("Update Assignment", new Color(255, 140, 0), "update");
        JButton completeBtn = createActionButton("Mark Completed", new Color(0, 120, 215), "complete");
        JButton setTimingBtn = createActionButton("Set Timing", new Color(155, 100, 200), "timing");
        JButton refreshBtn = createActionButton("Refresh List", new Color(100, 130, 200), "refresh");
        JButton historyBtn = createActionButton("View History", new Color(150, 150, 150), "history");

        buttonPanel.add(assignBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(completeBtn);
        buttonPanel.add(setTimingBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(historyBtn);

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(245, 247, 250));
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(200, 80, 80));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            main.setCurrentEmployeeId(null);
            main.setGarageLoggedIn(false);
            main.showPage("LOGIN");
        });
        logoutPanel.add(logoutBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(245, 247, 250));
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(logoutPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // selection listener
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFieldsFromSelectedRow();
            }
        });

        // initial load
        loadGarageRequests();
    }

    // Called from MainPortal.showPage("GARAGE_DASH")
    public void refreshFromSelectedId() {
        loadGarageRequests();
        String preselectId = main.getSelectedGarageRequestId();
        if (preselectId != null && !preselectId.isEmpty()) {
            selectRowByRequestId(preselectId);
        }
        populateFieldsFromSelectedRow();
    }

    private JButton createHeaderButton(String text, Color bgColor) {
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

    private void selectRowByRequestId(String requestId) {
        if (requestId == null || requestId.isEmpty()) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = (String) tableModel.getValueAt(i, 0);
            if (requestId.equals(id)) {
                requestTable.setRowSelectionInterval(i, i);
                requestTable.scrollRectToVisible(requestTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private void loadGarageRequests() {
        tableModel.setRowCount(0);

        List<String[]> requests = DatabaseOperations.getGarageRequests();
        int assignedCount = 0;
        int pendingAssignmentCount = 0;
        int completedCount = 0;

        for (String[] req : requests) {
            String status = req[8];

            if ("Assigned".equals(status)) {
                assignedCount++;
            } else if ("Completed".equals(status)) {
                completedCount++;
            } else if (status != null && status.contains("Approved")) {
                pendingAssignmentCount++;
            }

            String pickupDate = "-";
            String pickupTime = "-";
            String dropoffDate = "-";
            String dropoffTime = "-";

            try {
                if (req[13] != null && !req[13].isEmpty()) {
                    Date pickupDateTime = dbDateTimeFormat.parse(req[13]);
                    pickupDate = displayDateFormat.format(pickupDateTime);
                    pickupTime = displayTimeFormat.format(pickupDateTime);
                }

                if (req[14] != null && !req[14].isEmpty()) {
                    Date dropoffDateTime = dbDateTimeFormat.parse(req[14]);
                    dropoffDate = displayDateFormat.format(dropoffDateTime);
                    dropoffTime = displayTimeFormat.format(dropoffDateTime);
                }
            } catch (ParseException e) {
                // ignore
            }

            String tripCompleted = "1".equals(req[15]) ? "✓ Yes" : "✗ No";

            tableModel.addRow(new Object[]{
                    req[0],
                    req[1] + " - " + truncateText(req[2], 15),
                    truncateText(req[5], 10),
                    truncateText(req[6], 10),
                    truncateText(req[7], 20),
                    status,
                    pickupDate,
                    pickupTime,
                    dropoffDate,
                    dropoffTime,
                    nullToDash(req[9]),
                    nullToDash(req[10]),
                    nullToDash(req[11]),
                    nullToDash(req[12]),
                    tripCompleted
            });
        }

        statusLabel.setText(
                assignedCount + " assigned | " +
                        pendingAssignmentCount + " pending assignment | " +
                        completedCount + " completed | " +
                        tableModel.getRowCount() + " total requests"
        );
    }

    private void populateFieldsFromSelectedRow() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            clearAllFields();
            return;
        }

        String vehicleName = (String) tableModel.getValueAt(row, 10);
        String vehicleNumber = (String) tableModel.getValueAt(row, 11);
        String driverName = (String) tableModel.getValueAt(row, 12);
        String driverContact = (String) tableModel.getValueAt(row, 13);
        String completed = (String) tableModel.getValueAt(row, 14);
        String pickupDate = (String) tableModel.getValueAt(row, 6);
        String pickupTime = (String) tableModel.getValueAt(row, 7);
        String dropoffDate = (String) tableModel.getValueAt(row, 8);
        String dropoffTime = (String) tableModel.getValueAt(row, 9);

        vehicleNameField.setText("-".equals(vehicleName) ? "" : vehicleName);
        vehicleNumberField.setText("-".equals(vehicleNumber) ? "" : vehicleNumber);
        driverNameField.setText("-".equals(driverName) ? "" : driverName);
        driverContactField.setText("-".equals(driverContact) ? "" : driverContact);
        tripCompletedCheck.setSelected("✓ Yes".equals(completed));

        pickupDateField.setText("-".equals(pickupDate) ? "" : pickupDate);
        pickupTimeField.setText("-".equals(pickupTime) ? "" : pickupTime);
        dropoffDateField.setText("-".equals(dropoffDate) ? "" : dropoffDate);
        dropoffTimeField.setText("-".equals(dropoffTime) ? "" : dropoffTime);

        String status = (String) tableModel.getValueAt(row, 5);
        vehicleAvailableCheck.setSelected(!"Assigned".equals(status) && !"Completed".equals(status));

        String requestId = (String) tableModel.getValueAt(row, 0);
        String[] reqDetails = DatabaseOperations.getVehicleRequestDetails(requestId);
        if (reqDetails != null) {
            intenderNameField.setText(reqDetails[2]);        // emp_name
            intenderDeptField.setText(reqDetails[4]);        // dept_name
            intenderDesignationField.setText(reqDetails[3]); // designation
            String contact = reqDetails.length >= 17 ? reqDetails[16] : null; // requester_contact
            intenderRequestContactField.setText(
                    contact != null && !contact.isEmpty() ? contact : "NA");
        } else {
            clearIntenderDetails();
        }
    }

    private void clearAllFields() {
        clearIntenderDetails();
        clearVehicleDetails();
    }

    private void clearIntenderDetails() {
        intenderNameField.setText("");
        intenderDeptField.setText("");
        intenderDesignationField.setText("");
        intenderRequestContactField.setText("");
    }

    private void clearVehicleDetails() {
        vehicleNameField.setText("");
        vehicleNumberField.setText("");
        driverNameField.setText("");
        driverContactField.setText("");
        pickupDateField.setText("");
        pickupTimeField.setText("");
        dropoffDateField.setText("");
        dropoffTimeField.setText("");
        vehicleAvailableCheck.setSelected(true);
        tripCompletedCheck.setSelected(false);
    }

    private JButton createActionButton(String text, Color color, String action) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        switch (action) {
            case "assign":
            case "update":
                btn.addActionListener(e -> assignOrUpdateVehicle());
                break;
            case "complete":
                btn.addActionListener(e -> markTripCompleted());
                break;
            case "refresh":
                btn.addActionListener(e -> refreshFromSelectedId());
                break;
            case "timing":
                btn.addActionListener(e -> setTimingDetails());
                break;
            case "history":
                btn.addActionListener(e -> main.showPage("GARAGE_HISTORY"));
                break;
        }
        return btn;
    }

    // === assign/update/complete/timing logic ===

    private void assignOrUpdateVehicle() {
        int row = requestTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!vehicleAvailableCheck.isSelected()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Vehicle is marked as not available. Continue anyway?",
                    "Vehicle Availability", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) return;
        }

        String vehicleName = vehicleNameField.getText().trim();
        String vehicleNumber = vehicleNumberField.getText().trim();
        String driverName = driverNameField.getText().trim();
        String driverContact = driverContactField.getText().trim();
        String pickupDate = pickupDateField.getText().trim();
        String pickupTime = pickupTimeField.getText().trim();
        String dropoffDate = dropoffDateField.getText().trim();
        String dropoffTime = dropoffTimeField.getText().trim();

        if (vehicleName.isEmpty() || vehicleNumber.isEmpty() ||
                driverName.isEmpty() || driverContact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all vehicle and driver details.",
                    "Incomplete Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputFields(pickupDate, pickupTime, dropoffDate, dropoffTime)) {
            return;
        }

        Timestamp pickupDateTime = null;
        Timestamp dropoffDateTime = null;

        try {
            if (!pickupDate.isEmpty() && !pickupTime.isEmpty()) {
                Date date = inputDateTimeFormat.parse(pickupDate + " " + pickupTime);
                pickupDateTime = new Timestamp(date.getTime());
            }

            if (!dropoffDate.isEmpty() && !dropoffTime.isEmpty()) {
                Date date = inputDateTimeFormat.parse(dropoffDate + " " + dropoffTime);
                dropoffDateTime = new Timestamp(date.getTime());
            }

            if (pickupDateTime != null && dropoffDateTime != null &&
                    dropoffDateTime.before(pickupDateTime)) {
                JOptionPane.showMessageDialog(this,
                        "Drop-off datetime must be after pickup datetime.",
                        "Invalid Timing", JOptionPane.WARNING_MESSAGE);
                return;
            }

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error parsing datetime. Please check the format.\n" +
                            "Date: DD-MM-YYYY, Time: HH:MM",
                    "DateTime Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String requestId = (String) tableModel.getValueAt(row, 0);
        String assignedBy = main.getCurrentEmployeeId() != null
                ? main.getCurrentEmployeeId()
                : "garage";

        boolean success = DatabaseOperations.updateGarageAssignment(
                requestId, vehicleName, vehicleNumber, driverName, driverContact,
                pickupDateTime, dropoffDateTime, tripCompletedCheck.isSelected(), assignedBy);

        if (success) {
            updateTableRowAfterAssignment(row, pickupDateTime, dropoffDateTime,
                    vehicleName, vehicleNumber, driverName, driverContact,
                    tripCompletedCheck.isSelected());

            String existingVehicle = (String) tableModel.getValueAt(row, 10);
            boolean isNew = (existingVehicle == null || "-".equals(existingVehicle));

            String message = tripCompletedCheck.isSelected()
                    ? "Trip marked as completed!"
                    : "Vehicle assignment " + (isNew ? "created" : "updated") + " successfully!";

            String timingInfo = buildTimingInfoMessage(pickupDateTime, dropoffDateTime);

            JOptionPane.showMessageDialog(this,
                    message + "\n\nVehicle: " + vehicleName +
                            "\nNumber: " + vehicleNumber +
                            "\nDriver: " + driverName +
                            (timingInfo.isEmpty() ? "" : "\n\n" + timingInfo),
                    tripCompletedCheck.isSelected() ? "Trip Completed" : "Assignment Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            refreshFromSelectedId();

        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update assignment. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInputFields(String pickupDate, String pickupTime,
                                        String dropoffDate, String dropoffTime) {
        // Basic validation: allow empty (no timing yet), else check patterns if needed.
        // You can add regex/date checks here if you want stricter validation.
        return true;
    }

    private void updateTableRowAfterAssignment(int row,
                                               Timestamp pickupDateTime,
                                               Timestamp dropoffDateTime,
                                               String vehicleName,
                                               String vehicleNumber,
                                               String driverName,
                                               String driverContact,
                                               boolean completed) {
        String pickupDate = "-";
        String pickupTime = "-";
        String dropoffDate = "-";
        String dropoffTime = "-";

        if (pickupDateTime != null) {
            Date d = new Date(pickupDateTime.getTime());
            pickupDate = displayDateFormat.format(d);
            pickupTime = displayTimeFormat.format(d);
        }

        if (dropoffDateTime != null) {
            Date d = new Date(dropoffDateTime.getTime());
            dropoffDate = displayDateFormat.format(d);
            dropoffTime = displayTimeFormat.format(d);
        }

        tableModel.setValueAt(pickupDate, row, 6);
        tableModel.setValueAt(pickupTime, row, 7);
        tableModel.setValueAt(dropoffDate, row, 8);
        tableModel.setValueAt(dropoffTime, row, 9);
        tableModel.setValueAt(vehicleName, row, 10);
        tableModel.setValueAt(vehicleNumber, row, 11);
        tableModel.setValueAt(driverName, row, 12);
        tableModel.setValueAt(driverContact, row, 13);
        tableModel.setValueAt(completed ? "✓ Yes" : "✗ No", row, 14);
        tableModel.setValueAt(completed ? "Completed" : "Assigned", row, 5);
    }

    private String buildTimingInfoMessage(Timestamp pickup, Timestamp dropoff) {
        StringBuilder sb = new StringBuilder();
        if (pickup != null) {
            sb.append("Pickup: ")
                    .append(displayDateFormat.format(new Date(pickup.getTime())))
                    .append(" ")
                    .append(displayTimeFormat.format(new Date(pickup.getTime())));
        }
        if (dropoff != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("Drop-off: ")
                    .append(displayDateFormat.format(new Date(dropoff.getTime())))
                    .append(" ")
                    .append(displayTimeFormat.format(new Date(dropoff.getTime())));
        }
        return sb.toString();
    }

    private void markTripCompleted() {
        // Simple helper: mark checkbox and reuse assign/update logic
        tripCompletedCheck.setSelected(true);
        assignOrUpdateVehicle();
    }

    private void setTimingDetails() {
        // Optional: pre-fill pickup/drop from current time or show dialog.
        // For now this is a no-op or you can add a simple dialog here.
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTextField createReadOnlyTextField() {
        JTextField field = new JTextField(15);
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(248, 248, 248));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
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
