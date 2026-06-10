// VehicleRequisitionFormPanel.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import com.toedter.calendar.JDateChooser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleRequisitionFormPanel extends JPanel {
    private final MainPortal main;
    private final String sourceRole;
    private final String empId;

    private JLabel logoLabel;
    private JTextField empNoField, nameField, designationField, deptNoField, deptNameField;
    private JTextArea purposeArea, pickupArea;
    private JTextField beyondLocationField, trainField, contactField;
    private JComboBox<String> workTypeCombo, beyondCombo, peopleCombo;
    private JSpinner peopleCountSpinner, requestTimeSpinner, expectedTimeSpinner;
    private JDateChooser requestDateChooser, expectedDateChooser;
    private JLabel formTitleLabel;
    private JScrollPane mainScrollPane;

    public VehicleRequisitionFormPanel(MainPortal main, String sourceRole, String empId) {
        this.main = main;
        this.sourceRole = sourceRole;
        this.empId = empId;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        JPanel mainFormPanel = createMainFormPanel();
        mainScrollPane = new JScrollPane(mainFormPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getViewport().setBackground(new Color(245, 247, 250));

        JScrollBar verticalScrollBar = mainScrollPane.getVerticalScrollBar();
        verticalScrollBar.setPreferredSize(new Dimension(10, 0));
        verticalScrollBar.setBackground(new Color(245, 247, 250));
        verticalScrollBar.setForeground(new Color(0, 70, 140));

        add(mainScrollPane, BorderLayout.CENTER);

        // Auto-fill employee details FROM DATABASE
        autoFillEmployeeDetails();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 70, 140));
        topPanel.setPreferredSize(new Dimension(0, 90));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JButton backBtn = createHeaderButton("Back", Color.BLACK);
        backBtn.addActionListener(e -> {
            // FIXED: use real card names and handle role properly
            if ("ADMIN".equalsIgnoreCase(sourceRole)) {
                // form opened from Admin dashboard
                main.showPage("ADMIN_DASH");
            } else if ("EMPLOYEE".equalsIgnoreCase(sourceRole) || "EMP".equalsIgnoreCase(sourceRole)) {
                // form opened from Employee dashboard
                main.showPage("EMP_DASH");
            } else {
                // Fallback: if current role in MainPortal is Admin or Employee, use that
                String role = main.getCurrentRole();
                if ("Admin".equalsIgnoreCase(role)) {
                    main.showPage("ADMIN_DASH");
                } else if ("Employee".equalsIgnoreCase(role)) {
                    main.showPage("EMP_DASH");
                } else {
                    // last fallback: go to login
                    main.showPage("LOGIN");
                }
            }
        });
        topPanel.add(backBtn, BorderLayout.WEST);

        formTitleLabel = new JLabel("VEHICLE REQUISITION FORM", SwingConstants.CENTER);
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitleLabel.setForeground(Color.BLACK);
        topPanel.add(formTitleLabel, BorderLayout.CENTER);

        try {
            ImageIcon icon = new ImageIcon("OrientLogo.png");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(120, 45, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(img));
                logoLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 30));
                topPanel.add(logoLabel, BorderLayout.EAST);
            } else {
                logoLabel = new JLabel("ORIENT");
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                logoLabel.setForeground(Color.BLACK);
                logoLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 30));
                topPanel.add(logoLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            logoLabel = new JLabel("ORIENT");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            logoLabel.setForeground(Color.BLACK);
            logoLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 30));
            topPanel.add(logoLabel, BorderLayout.EAST);
        }

        return topPanel;
    }

    private JPanel createMainFormPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        columnsPanel.setBackground(new Color(245, 247, 250));
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel leftPanel = createLeftPanel();
        columnsPanel.add(leftPanel);

        JPanel rightPanel = createRightPanel();
        columnsPanel.add(rightPanel);

        mainPanel.add(columnsPanel);

        JPanel submitPanel = createSubmitPanel();
        mainPanel.add(submitPanel);

        return mainPanel;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel sectionTitle = new JLabel("Travel Purpose Details");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(0, 70, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(sectionTitle, gbc);

        // Nature of Work
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(createFormLabel("Nature of Work"), gbc);
        gbc.gridx = 1;
        workTypeCombo = new JComboBox<>(new String[]{"Company Work", "Personal Work"});
        styleCombo(workTypeCombo);
        panel.add(workTypeCombo, gbc);

        // Purpose
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(createFormLabel("Purpose of Travel"), gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        purposeArea = new JTextArea(3, 20);
        styleTextArea(purposeArea);
        purposeArea.setToolTipText("Describe the purpose of your travel");
        JScrollPane purposeScroll = new JScrollPane(purposeArea);
        purposeScroll.setPreferredSize(new Dimension(0, 80));
        panel.add(purposeScroll, gbc);

        // Pickup Address
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(createFormLabel("Pickup Address"), gbc);
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.2;
        pickupArea = new JTextArea(2, 20);
        styleTextArea(pickupArea);
        pickupArea.setToolTipText("Enter pickup location address");
        JScrollPane pickupScroll = new JScrollPane(pickupArea);
        pickupScroll.setPreferredSize(new Dimension(0, 60));
        panel.add(pickupScroll, gbc);

        // Beyond Anuppur/Shahdol
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(createFormLabel("Beyond Anuppur/Shahdol?"), gbc);
        gbc.gridy = 7;
        beyondCombo = new JComboBox<>(new String[]{"No", "Yes"});
        styleCombo(beyondCombo);
        beyondCombo.addActionListener(e -> beyondLocationField.setEnabled("Yes".equals(beyondCombo.getSelectedItem())));
        panel.add(beyondCombo, gbc);

        // If Yes, Location
        gbc.gridy = 8;
        panel.add(createFormLabel("If Yes, Specify Location"), gbc);
        gbc.gridy = 9;
        beyondLocationField = new JTextField(20);
        styleField(beyondLocationField);
        beyondLocationField.setEnabled(false);
        panel.add(beyondLocationField, gbc);

        // No. of People
        gbc.gridy = 10;
        panel.add(createFormLabel("Number of People"), gbc);
        gbc.gridy = 11;
        peopleCombo = new JComboBox<>(new String[]{"Single", "With Family"});
        styleCombo(peopleCombo);
        peopleCombo.addActionListener(e -> peopleCountSpinner.setEnabled("With Family".equals(peopleCombo.getSelectedItem())));
        panel.add(peopleCombo, gbc);

        // If Family, Count
        gbc.gridy = 12;
        panel.add(createFormLabel("Family Members Count"), gbc);
        gbc.gridy = 13;
        peopleCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        styleSpinner(peopleCountSpinner);
        peopleCountSpinner.setEnabled(false);
        panel.add(peopleCountSpinner, gbc);

        // Train Details
        gbc.gridy = 14;
        panel.add(createFormLabel("Train Details (if applicable)"), gbc);
        gbc.gridy = 15;
        trainField = new JTextField(20);
        styleField(trainField);
        panel.add(trainField, gbc);

        // Contact No
        gbc.gridy = 16;
        panel.add(createFormLabel("Contact Number"), gbc);
        gbc.gridy = 17;
        contactField = new JTextField(20);
        styleField(contactField);
        contactField.setToolTipText("Enter your contact number for emergencies");
        panel.add(contactField, gbc);

        // Filler
        gbc.gridy = 18;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel sectionTitle = new JLabel("Employee & Date Details");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(new Color(0, 70, 140));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(sectionTitle, gbc);

        // Employee No
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(createFormLabel("Employee Number"), gbc);
        gbc.gridx = 1;
        empNoField = new JTextField(15);
        styleField(empNoField);
        empNoField.setEditable(false);
        empNoField.setBackground(new Color(248, 248, 248));
        panel.add(empNoField, gbc);

        // Intender Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(createFormLabel("Intender Name"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        styleField(nameField);
        nameField.setEditable(false);
        nameField.setBackground(new Color(248, 248, 248));
        panel.add(nameField, gbc);

        // Designation
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(createFormLabel("Designation"), gbc);
        gbc.gridx = 1;
        designationField = new JTextField(15);
        styleField(designationField);
        designationField.setEditable(false);
        designationField.setBackground(new Color(248, 248, 248));
        panel.add(designationField, gbc);

        // Department No
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(createFormLabel("Department Code"), gbc);
        gbc.gridx = 1;
        deptNoField = new JTextField(15);
        styleField(deptNoField);
        deptNoField.setEditable(false);
        deptNoField.setBackground(new Color(248, 248, 248));
        panel.add(deptNoField, gbc);

        // Department Name
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(createFormLabel("Department Name"), gbc);
        gbc.gridx = 1;
        deptNameField = new JTextField(15);
        styleField(deptNameField);
        deptNameField.setEditable(false);
        deptNameField.setBackground(new Color(248, 248, 248));
        panel.add(deptNameField, gbc);

        // Divider
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(220, 220, 220));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 0, 20, 0);
        panel.add(separator, gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        // Request Date
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(createFormLabel("Request Date"), gbc);
        gbc.gridx = 1;
        requestDateChooser = new JDateChooser(new Date());
        requestDateChooser.setMinSelectableDate(new Date());
        requestDateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        requestDateChooser.setPreferredSize(new Dimension(150, 30));
        panel.add(requestDateChooser, gbc);

        // Form Filling Time
        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(createFormLabel("Form Filling Time"), gbc);
        gbc.gridx = 1;
        requestTimeSpinner = createTimeSpinner();
        panel.add(requestTimeSpinner, gbc);

        // Expected Date
        gbc.gridx = 0;
        gbc.gridy = 9;
        panel.add(createFormLabel("Expected Travel Date"), gbc);
        gbc.gridx = 1;
        expectedDateChooser = new JDateChooser(new Date());
        expectedDateChooser.setMinSelectableDate(new Date());
        expectedDateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        expectedDateChooser.setPreferredSize(new Dimension(150, 30));
        panel.add(expectedDateChooser, gbc);

        // Expected Departure Time
        gbc.gridx = 0;
        gbc.gridy = 10;
        panel.add(createFormLabel("Expected Departure Time"), gbc);
        gbc.gridx = 1;
        expectedTimeSpinner = createTimeSpinner();
        panel.add(expectedTimeSpinner, gbc);

        // Filler
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createSubmitPanel() {
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        submitPanel.setBackground(new Color(245, 247, 250));
        submitPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        submitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton submitBtn = createSubmitButton("SUBMIT REQUEST");
        JButton clearBtn = createClearButton("CLEAR FORM");
        JButton previewBtn = createPreviewButton("PREVIEW");

        submitBtn.addActionListener(e -> submitForm());
        clearBtn.addActionListener(e -> clearForm());
        previewBtn.addActionListener(e -> previewForm());

        submitPanel.add(previewBtn);
        submitPanel.add(clearBtn);
        submitPanel.add(submitBtn);

        return submitPanel;
    }

    // ===== DB-AWARE METHODS =====

    private void autoFillEmployeeDetails() {
        if (empId != null && !"ADMIN".equalsIgnoreCase(sourceRole)) {
            try {
                String sql = "SELECT emp_id, emp_name, designation, dept_code, dept_name " +
                             "FROM employees WHERE emp_id = ? AND is_active = TRUE";
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, empId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String id = rs.getString("emp_id");
                    String name = rs.getString("emp_name");
                    String desig = rs.getString("designation");
                    String deptCode = rs.getString("dept_code");
                    String deptName = rs.getString("dept_name");

                    empNoField.setText(id);
                    nameField.setText(name);
                    designationField.setText(desig);
                    deptNoField.setText(deptCode);
                    deptNameField.setText(deptName);
                    formTitleLabel.setText("VEHICLE REQUISITION FORM - " + name.toUpperCase());
                }

                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading employee details: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else if ("ADMIN".equalsIgnoreCase(sourceRole)) {
            empNoField.setEditable(true);
            empNoField.setBackground(Color.WHITE);
            formTitleLabel.setText("VEHICLE REQUISITION FORM - ADMIN MODE");
        }
    }

    private void submitForm() {
        if (!validateForm()) return;

        String empNo = empNoField.getText().trim();

        if ("ADMIN".equalsIgnoreCase(sourceRole)) {
            try {
                String checkSql = "SELECT emp_id FROM employees WHERE emp_id = ? AND is_active = TRUE";
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, empNo);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "Employee ID " + empNo + " not found in system.",
                            "Invalid Employee", JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    checkStmt.close();
                    conn.close();
                    return;
                }
                rs.close();
                checkStmt.close();
                conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error checking employee ID: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
        }

        String fromLocation = pickupArea.getText().trim().isEmpty()
                ? "Plant" : pickupArea.getText().trim();
        String beyondLocation = beyondLocationField.getText().trim();
        String toLocation = beyondLocation.isEmpty()
                ? "Anuppur/Shahdol Area" : beyondLocation;
        String purpose = purposeArea.getText().trim();
        String natureOfWork = (String) workTypeCombo.getSelectedItem();
        String beyondArea = (String) beyondCombo.getSelectedItem();
        String travelersType = (String) peopleCombo.getSelectedItem();
        int familyCount = (Integer) peopleCountSpinner.getValue();
        String trainDetails = trainField.getText().trim();
        String contactNumber = contactField.getText().trim();

        boolean ok = DatabaseOperations.submitVehicleRequest(
                empNo,
                fromLocation,
                toLocation,
                purpose,
                natureOfWork,
                beyondArea,
                beyondLocation,
                travelersType,
                familyCount,
                trainDetails,
                contactNumber
        );

        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Error submitting request. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String successMessage =
                "<html><div style='font-family: Segoe UI; padding: 10px;'>" +
                "<h3 style='color: #00468c'>Request Submitted Successfully!</h3>" +
                "<b>Employee:</b> " + empNo + " - " + nameField.getText() + "<br>" +
                "<b>Purpose:</b> " + truncateText(purpose, 50) + "<br>" +
                "<b>Destination:</b> " + toLocation + "<br>" +
                "<b>Next Step:</b> " +
                ("Yes".equals(beyondArea) ? "Awaiting HOD & COO approval" : "Awaiting HOD approval") +
                "</div></html>";

        JOptionPane.showMessageDialog(this, successMessage,
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);

        clearForm();
    }

    private void clearForm() {
        purposeArea.setText("");
        pickupArea.setText("");
        beyondCombo.setSelectedIndex(0);
        beyondLocationField.setText("");
        beyondLocationField.setEnabled(false);
        peopleCombo.setSelectedIndex(0);
        peopleCountSpinner.setValue(1);
        peopleCountSpinner.setEnabled(false);
        trainField.setText("");
        contactField.setText("");
        requestDateChooser.setDate(new Date());
        expectedDateChooser.setDate(new Date());
        SwingUtilities.invokeLater(() -> {
            if (mainScrollPane != null) {
                mainScrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (purposeArea.getText().trim().isEmpty()) {
            errors.append("- Purpose of travel is required.\n");
        }
        if (contactField.getText().trim().isEmpty()) {
            errors.append("- Contact number is required.\n");
        } else if (!contactField.getText().trim().matches("\\d{10}")) {
            errors.append("- Contact number must be 10 digits.\n");
        }
        if ("Yes".equals(beyondCombo.getSelectedItem()) &&
                beyondLocationField.getText().trim().isEmpty()) {
            errors.append("- Please specify location for travel beyond Anuppur/Shahdol.\n");
        }
        if (empNoField.getText().trim().isEmpty()) {
            errors.append("- Employee number is required.\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Please correct the following errors:\n\n" + errors,
                    "Validation Errors", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void previewForm() {
        if (!validateForm()) return;

        String preview =
                "<html><div style='font-family: Segoe UI; padding: 10px; width: 400px;'>" +
                "<h3 style='color: #00468c'>Request Preview</h3>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr><td style='padding: 5px; font-weight: bold; width: 40%;'>Employee</td>" +
                "<td style='padding: 5px;'>" + empNoField.getText() + " - " + nameField.getText() + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Purpose</td>" +
                "<td style='padding: 5px;'>" + purposeArea.getText() + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Pickup</td>" +
                "<td style='padding: 5px;'>" +
                (pickupArea.getText().isEmpty() ? "Plant" : pickupArea.getText()) + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Destination</td>" +
                "<td style='padding: 5px;'>" +
                ("Yes".equals(beyondCombo.getSelectedItem()) ?
                        beyondLocationField.getText() : "Anuppur/Shahdol Area") +
                "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Travelers</td>" +
                "<td style='padding: 5px;'>" + peopleCombo.getSelectedItem() +
                ("With Family".equals(peopleCombo.getSelectedItem()) ?
                        " (" + peopleCountSpinner.getValue() + ")" : "") +
                "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Contact</td>" +
                "<td style='padding: 5px;'>" + contactField.getText() + "</td></tr>" +
                "<tr><td style='padding: 5px; font-weight: bold;'>Approval Required</td>" +
                "<td style='padding: 5px;'>" +
                ("Yes".equals(beyondCombo.getSelectedItem()) ? "HOD & COO" : "HOD Only") +
                "</td></tr>" +
                "</table></div></html>";

        int result = JOptionPane.showConfirmDialog(this, preview,
                "Preview Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            submitForm();
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        spinner.setPreferredSize(new Dimension(150, 30));
        return spinner;
    }

    private JButton createHeaderButton(String text, Color textColor) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 70, 140));
        btn.setForeground(textColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSubmitButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 150, 70));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 130, 50), 2),
                BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createClearButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(220, 80, 80));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createPreviewButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(255, 140, 0));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 120, 0), 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
