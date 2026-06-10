import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewEmployeesPanel extends JPanel {
    private final MainPortal main;
    private JTable empTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel statusLabel;

    // Form fields
    private JTextField noField, nameField, desigField, deptNoField, deptNameField, pwdField;
    private JTextField searchField;

    // Multi-select buttons
    private JButton selectAllBtn, deselectAllBtn, deleteSelectedBtn;

    // NEW: CSV import button
    private JButton importCsvBtn;

    public ViewEmployeesPanel(MainPortal main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // TOP PANEL WITH LOGO
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton backBtn = createStyledButton("Back", new Color(100, 130, 200));
        backBtn.addActionListener(e -> main.showPage("ADMIN_DASH"));
        topPanel.add(backBtn, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("EMPLOYEE MANAGEMENT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 70, 140));
        titlePanel.add(title, BorderLayout.CENTER);

        statusLabel = new JLabel("Manage employee database", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(statusLabel, BorderLayout.SOUTH);
        topPanel.add(titlePanel, BorderLayout.CENTER);

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

        // MAIN CONTENT
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(new Color(245, 247, 250));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // SEARCH PANEL
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        JLabel searchLabel = new JLabel("Search Employees");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(new Color(0, 70, 140));
        searchPanel.add(searchLabel, BorderLayout.WEST);

        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setToolTipText("Search by Employee ID or Name...");
        JPanel searchFieldPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchFieldPanel.setBackground(Color.WHITE);
        searchFieldPanel.add(searchField);
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        mainContent.add(searchPanel, BorderLayout.NORTH);

        // MULTI-SELECT BUTTON PANEL
        JPanel multiSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        multiSelectPanel.setBackground(new Color(245, 247, 250));
        multiSelectPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JLabel multiSelectLabel = new JLabel("Multi-select Actions");
        multiSelectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        multiSelectLabel.setForeground(new Color(0, 70, 140));
        multiSelectPanel.add(multiSelectLabel);

        selectAllBtn = createMultiSelectButton("Select All", new Color(70, 150, 70));
        deselectAllBtn = createMultiSelectButton("Deselect All", new Color(220, 80, 80));
        deleteSelectedBtn = createMultiSelectButton("Delete Selected", new Color(150, 0, 0));

        selectAllBtn.addActionListener(e -> selectAllEmployees());
        deselectAllBtn.addActionListener(e -> deselectAllEmployees());
        deleteSelectedBtn.addActionListener(e -> deleteSelectedEmployees());

        multiSelectPanel.add(selectAllBtn);
        multiSelectPanel.add(deselectAllBtn);
        multiSelectPanel.add(deleteSelectedBtn);

        // TABLE PANEL
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));

        // TABLE SETUP
        String[] columns = {"Select", "Employee ID", "Full Name", "Designation", "Department", "Dept Code"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only the checkbox column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Boolean.class; // Checkbox column
                return String.class; // Other columns
            }
        };

        empTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                return c;
            }
        };

        // Enable multi-selection
        empTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        empTable.setRowHeight(35);
        empTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        empTable.setShowGrid(true);
        empTable.setGridColor(new Color(230, 230, 230));
        empTable.setSelectionBackground(new Color(200, 220, 255));
        empTable.setSelectionForeground(Color.BLACK);

        // Set column widths
        empTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Select checkbox
        empTable.getColumnModel().getColumn(0).setMaxWidth(60);
        empTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Emp ID
        empTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Name
        empTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Designation
        empTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Department
        empTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Dept Code

        rowSorter = new TableRowSorter<>(tableModel);
        empTable.setRowSorter(rowSorter);

        // Custom header
        JTableHeader header = empTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 90, 160));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(empTable);
        scrollPane.setPreferredSize(new Dimension(800, 250));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // wrapper for table + multi-select
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(new Color(245, 247, 250));
        tableWrapper.add(multiSelectPanel, BorderLayout.NORTH);
        tableWrapper.add(tablePanel, BorderLayout.CENTER);
        mainContent.add(tableWrapper, BorderLayout.CENTER);

        // EMPLOYEE FORM PANEL
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("Add/Update Employee");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(0, 70, 140));
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        formPanel.add(formTitle, BorderLayout.NORTH);

        // Form fields in grid
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(createFormLabel("Employee ID"), gbc);
        gbc.gridx = 1;
        noField = createFormTextField(15);
        fieldsPanel.add(noField, gbc);
        gbc.gridx = 2;
        fieldsPanel.add(createFormLabel("Full Name"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 2;
        nameField = createFormTextField(25);
        fieldsPanel.add(nameField, gbc);
        gbc.gridwidth = 1;

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(createFormLabel("Designation"), gbc);
        gbc.gridx = 1;
        desigField = createFormTextField(15);
        fieldsPanel.add(desigField, gbc);
        gbc.gridx = 2;
        fieldsPanel.add(createFormLabel("Department Code"), gbc);
        gbc.gridx = 3;
        deptNoField = createFormTextField(10);
        fieldsPanel.add(deptNoField, gbc);
        gbc.gridx = 4;
        fieldsPanel.add(createFormLabel("Password"), gbc);
        gbc.gridx = 5;
        pwdField = createFormTextField(12);
        pwdField.setToolTipText("Set employee login password");
        fieldsPanel.add(pwdField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(createFormLabel("Department Name"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        deptNameField = createFormTextField(30);
        fieldsPanel.add(deptNameField, gbc);
        gbc.gridwidth = 1;

        // Buttons (including Import CSV)
        gbc.gridx = 4; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton addBtn = createFormButton("Save Employee", new Color(70, 150, 70));
        JButton clearBtn = createFormButton("Clear Form", new Color(220, 80, 80));
        JButton removeBtn = createFormButton("Remove Selected", new Color(150, 0, 0));
        importCsvBtn = createFormButton("Import CSV", new Color(0, 120, 215));

        addBtn.addActionListener(e -> handleAddOrUpdate());
        clearBtn.addActionListener(e -> clearForm());
        removeBtn.addActionListener(e -> handleRemove());
        importCsvBtn.addActionListener(e -> importEmployeesFromCsv());

        buttonPanel.add(importCsvBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(removeBtn);
        fieldsPanel.add(buttonPanel, gbc);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        mainContent.add(formPanel, BorderLayout.SOUTH);
        add(mainContent, BorderLayout.CENTER);

        // EVENT HANDLERS
        // Search filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2)); // ID + Name
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
        });

        // Row selection listener
        empTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int[] selectedRows = empTable.getSelectedRows();
            if (selectedRows.length == 1) {
                int viewRow = selectedRows[0];
                int row = empTable.convertRowIndexToModel(viewRow);
                noField.setText(tableModel.getValueAt(row, 1).toString());
                nameField.setText(tableModel.getValueAt(row, 2).toString());
                desigField.setText(tableModel.getValueAt(row, 3).toString());
                deptNameField.setText(tableModel.getValueAt(row, 4).toString());
                deptNoField.setText(tableModel.getValueAt(row, 5).toString());
                pwdField.setText(""); // Clear password for security
            } else if (selectedRows.length > 1) {
                clearForm();
            }
        });

        // LOAD EMPLOYEES FROM DATABASE ON STARTUP
        loadEmployees();
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        try {
            String sql = "SELECT emp_id, emp_name, designation, dept_code, dept_name " +
                         "FROM employees WHERE is_active = TRUE ORDER BY emp_id";
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String empId = rs.getString("emp_id");
                    String empName = rs.getString("emp_name");
                    String designation = rs.getString("designation");
                    String deptCode = rs.getString("dept_code");
                    String deptName = rs.getString("dept_name");

                    tableModel.addRow(new Object[]{
                            false, empId, empName, designation, deptName, deptCode
                    });
                }
                rs.close();
                stmt.close();
                conn.close();

                statusLabel.setText(tableModel.getRowCount() + " employees loaded from database");
            } else {
                statusLabel.setText("Database connection failed");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error loading employees: " + e.getMessage());
            e.printStackTrace();
        }
        updateSelectedCount();
    }

    private void selectAllEmployees() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(true, i, 0);
        }
        updateSelectedCount();
    }

    private void deselectAllEmployees() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, 0);
        }
        updateSelectedCount();
    }

    // Delete selected employees (soft delete: is_active = FALSE)
    private void deleteSelectedEmployees() {
        List<String> employeesToDelete = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                String empId = (String) tableModel.getValueAt(i, 1);
                employeesToDelete.add(empId);
            }
        }

        if (employeesToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees selected for deletion.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int count = employeesToDelete.size();
        String message = count == 1 ?
                "Delete employee " + employeesToDelete.get(0) + "?" :
                "Delete " + count + " selected employees?";

        int confirm = JOptionPane.showConfirmDialog(this,
                message + "\nThis action cannot be undone.",
                "Confirm Bulk Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);

                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE employees SET is_active = FALSE WHERE emp_id = ?");
                int deletedCount = 0;
                for (String empId : employeesToDelete) {
                    pstmt.setString(1, empId);
                    deletedCount += pstmt.executeUpdate();
                }
                pstmt.close();
                conn.commit();
                conn.close();

                loadEmployees();
                JOptionPane.showMessageDialog(this,
                        deletedCount + " employees marked inactive.",
                        "Deletion Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateSelectedCount() {
        int selectedCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) selectedCount++;
        }
        String baseStatus = tableModel.getRowCount() + " employees in database";
        if (selectedCount == 0) {
            statusLabel.setText(baseStatus);
        } else {
            statusLabel.setText(baseStatus + " (" + selectedCount + " selected)");
        }
    }

    // Add/Update employee to DATABASE
    private void handleAddOrUpdate() {
        String empNo = noField.getText().trim();
        String name = nameField.getText().trim();
        String desig = desigField.getText().trim();
        String deptNo = deptNoField.getText().trim();
        String deptName = deptNameField.getText().trim();
        String password = pwdField.getText().trim();

        if (empNo.isEmpty() || name.isEmpty() || desig.isEmpty() || deptNo.isEmpty() ||
                deptName.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields including password.",
                    "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (empNo.length() != 5 || !empNo.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "Employee ID must be exactly 5 digits.",
                    "Invalid ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT emp_id FROM employees WHERE emp_id = ?");
                checkStmt.setString(1, empNo);
                ResultSet rs = checkStmt.executeQuery();
                boolean exists = rs.next();
                rs.close();
                checkStmt.close();

                if (exists) {
                    String sql = "UPDATE employees SET emp_name = ?, designation = ?, " +
                                 "dept_code = ?, dept_name = ?, password_hash = MD5(?) " +
                                 "WHERE emp_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, name);
                    pstmt.setString(2, desig);
                    pstmt.setString(3, deptNo);
                    pstmt.setString(4, deptName);
                    pstmt.setString(5, password);
                    pstmt.setString(6, empNo);

                    int rows = pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Employee " + empNo + " updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    String sql = "INSERT INTO employees " +
                                 "(emp_id, emp_name, designation, dept_code, dept_name, " +
                                 "password_hash, role, is_active) " +
                                 "VALUES (?, ?, ?, ?, ?, MD5(?), 'Employee', TRUE)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, empNo);
                    pstmt.setString(2, name);
                    pstmt.setString(3, desig);
                    pstmt.setString(4, deptNo);
                    pstmt.setString(5, deptName);
                    pstmt.setString(6, password);

                    int rows = pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Employee " + empNo + " added successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                loadEmployees();
                clearForm();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleRemove() {
        String empNo = noField.getText().trim();
        if (empNo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee to remove or enter Employee ID.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove employee " + empNo + "? This action cannot be undone.",
                "Confirm Removal", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE employees SET is_active = FALSE WHERE emp_id = ?");
                pstmt.setString(1, empNo);
                int rows = pstmt.executeUpdate();
                pstmt.close();
                conn.close();

                if (rows > 0) {
                    loadEmployees();
                    clearForm();
                    JOptionPane.showMessageDialog(this,
                            "Employee removed successfully.",
                            "Removed", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Employee " + empNo + " not found.",
                            "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearForm() {
        noField.setText("");
        nameField.setText("");
        desigField.setText("");
        deptNoField.setText("");
        deptNameField.setText("");
        pwdField.setText("");
        empTable.clearSelection();
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private JTextField createFormTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JButton createFormButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMultiSelectButton(String text, Color color) {
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

    // NEW: CSV import handler
    private void importEmployeesFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Employee CSV File");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Selected file does not exist.",
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Import employees from:\n" + file.getAbsolutePath() +
                        "\n\nExpected columns (CSV):\n" +
                        "emp_id, emp_name, designation, dept_code, dept_name, password_plain, role, is_active",
                "Confirm Import",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        int processed = DatabaseOperations.bulkInsertEmployeesFromCsv(file);
        if (processed < 0) {
            JOptionPane.showMessageDialog(this,
                    "Error importing employees. See logs for details.",
                    "Import Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                processed + " rows processed from CSV.",
                "Import Complete",
                JOptionPane.INFORMATION_MESSAGE);

        loadEmployees();
    }
}
