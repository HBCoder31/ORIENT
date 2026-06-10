// DatabaseConnection.java - FIXED VERSION
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    // Update these credentials to match your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle_requisition_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";  // Change if you use different username
    private static final String PASSWORD = "admin";  // Your MySQL password
    
    // Alternative for no password:
    // private static final String PASSWORD = "";
    
    // For XAMPP default (no password):
    // private static final String PASSWORD = "";
    // private static final String URL = "jdbc:mysql://localhost:3306/vehicle_requisition_db?useSSL=false";

    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create connection with properties
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            
            Connection conn = DriverManager.getConnection(URL, props);
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("Please add mysql-connector-java-8.0.xx.jar to your classpath");
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    public static void createTables() {
        // First, ensure database exists
        createDatabaseIfNotExists();
        
        try (Connection conn = getConnection()) {
            System.out.println("✅ Connected to database successfully!");
            
            // Get database info
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database: " + meta.getDatabaseProductName());
            System.out.println("Version: " + meta.getDatabaseProductVersion());
            
            try (Statement stmt = conn.createStatement()) {
                // ===== DEPARTMENTS TABLE =====
                String createDepartmentsTable = "CREATE TABLE IF NOT EXISTS departments (" +
                        "dept_code VARCHAR(10) PRIMARY KEY, " +
                        "dept_name VARCHAR(100) NOT NULL, " +
                        "hod_name VARCHAR(100), " +
                        "hod_email VARCHAR(100), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.execute(createDepartmentsTable);
                System.out.println("✅ Departments table created/verified");

                // ===== EMPLOYEES TABLE =====
                String createEmployeesTable = "CREATE TABLE IF NOT EXISTS employees (" +
                        "emp_id VARCHAR(10) PRIMARY KEY, " +
                        "emp_name VARCHAR(100) NOT NULL, " +
                        "designation VARCHAR(50) NOT NULL, " +
                        "dept_code VARCHAR(10), " +
                        "dept_name VARCHAR(100), " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "contact_number VARCHAR(15), " +
                        "email VARCHAR(100), " +
                        "role ENUM('Employee', 'Admin', 'Garage', 'HOD', 'COO') DEFAULT 'Employee', " +
                        "is_active BOOLEAN DEFAULT TRUE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.execute(createEmployeesTable);
                System.out.println("✅ Employees table created/verified");

                // ===== VEHICLE REQUESTS TABLE =====
                String createVehicleRequestsTable = "CREATE TABLE IF NOT EXISTS vehicle_requests (" +
                        "request_id VARCHAR(20) PRIMARY KEY, " +
                        "emp_id VARCHAR(10) NOT NULL, " +
                        "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "from_location VARCHAR(200), " +
                        "to_location VARCHAR(200), " +
                        "purpose TEXT, " +
                        "nature_of_work ENUM('Company Work', 'Personal Work') DEFAULT 'Company Work', " +
                        "beyond_area ENUM('No', 'Yes') DEFAULT 'No', " +
                        "beyond_location VARCHAR(200), " +
                        "travelers_type ENUM('Single', 'With Family') DEFAULT 'Single', " +
                        "family_count INT DEFAULT 0, " +
                        "train_details VARCHAR(100), " +
                        "contact_number VARCHAR(15), " +
                        "status ENUM('Pending', 'Waiting for HOD Approval', 'Waiting for COO Approval', " +
                        "          'Approved by HOD', 'Approved (HOD & COO)', 'Rejected by HOD', " +
                        "          'Rejected by COO', 'Assigned', 'Completed', 'Cancelled') DEFAULT 'Pending', " +
                        "hod_approval_status ENUM('Pending', 'Approved', 'Rejected', 'Not Required') DEFAULT 'Pending', " +
                        "coo_approval_status ENUM('Pending', 'Approved', 'Rejected', 'Not Required') DEFAULT 'Not Required', " +
                        "hod_approval_date TIMESTAMP NULL, " +
                        "coo_approval_date TIMESTAMP NULL, " +
                        "hod_approver_id VARCHAR(10), " +
                        "coo_approver_id VARCHAR(10), " +
                        "vehicle_name VARCHAR(100), " +
                        "vehicle_number VARCHAR(20), " +
                        "driver_name VARCHAR(100), " +
                        "driver_contact VARCHAR(15), " +
                        "trip_completed BOOLEAN DEFAULT FALSE, " +
                        "pickup_datetime TIMESTAMP NULL, " +
                        "dropoff_datetime TIMESTAMP NULL)";
                stmt.execute(createVehicleRequestsTable);
                System.out.println("✅ Vehicle requests table created/verified");

                // ===== VEHICLE ASSIGNMENTS TABLE =====
                String createVehicleAssignmentsTable = "CREATE TABLE IF NOT EXISTS vehicle_assignments (" +
                        "assignment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "request_id VARCHAR(20) NOT NULL UNIQUE, " +
                        "vehicle_name VARCHAR(100) NOT NULL, " +
                        "vehicle_number VARCHAR(20) NOT NULL, " +
                        "driver_name VARCHAR(100) NOT NULL, " +
                        "driver_contact VARCHAR(15) NOT NULL, " +
                        "pickup_datetime TIMESTAMP NULL, " +
                        "dropoff_datetime TIMESTAMP NULL, " +
                        "assignment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "assigned_by VARCHAR(10), " +
                        "trip_completed BOOLEAN DEFAULT FALSE, " +
                        "completion_date TIMESTAMP NULL, " +
                        "remarks TEXT)";
                stmt.execute(createVehicleAssignmentsTable);
                System.out.println("✅ Vehicle assignments table created/verified");

                // ===== VEHICLES TABLE =====
                String createVehiclesTable = "CREATE TABLE IF NOT EXISTS vehicles (" +
                        "vehicle_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "vehicle_name VARCHAR(100) NOT NULL, " +
                        "vehicle_number VARCHAR(20) UNIQUE NOT NULL, " +
                        "vehicle_type ENUM('Car', 'SUV', 'Bus', 'Van', 'Truck') DEFAULT 'Car', " +
                        "capacity INT DEFAULT 4, " +
                        "fuel_type ENUM('Petrol', 'Diesel', 'Electric', 'CNG') DEFAULT 'Petrol', " +
                        "is_available BOOLEAN DEFAULT TRUE, " +
                        "last_service_date DATE, " +
                        "next_service_date DATE, " +
                        "current_location VARCHAR(200), " +
                        "driver_assigned_id VARCHAR(10), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
                stmt.execute(createVehiclesTable);
                System.out.println("✅ Vehicles table created/verified");

                // ===== DRIVERS TABLE =====
                String createDriversTable = "CREATE TABLE IF NOT EXISTS drivers (" +
                        "driver_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "driver_name VARCHAR(100) NOT NULL, " +
                        "contact_number VARCHAR(15) NOT NULL, " +
                        "license_number VARCHAR(50) UNIQUE NOT NULL, " +
                        "license_expiry DATE, " +
                        "address TEXT, " +
                        "is_available BOOLEAN DEFAULT TRUE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.execute(createDriversTable);
                System.out.println("✅ Drivers table created/verified");

                // ===== APPROVAL HISTORY TABLE =====
                String createApprovalHistoryTable = "CREATE TABLE IF NOT EXISTS approval_history (" +
                        "history_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "request_id VARCHAR(20) NOT NULL, " +
                        "approver_id VARCHAR(10) NOT NULL, " +
                        "approver_role ENUM('HOD', 'COO', 'Admin', 'Garage') NOT NULL, " +
                        "previous_status VARCHAR(50), " +
                        "new_status VARCHAR(50), " +
                        "approval_decision ENUM('Approved', 'Rejected', 'Assigned', 'Completed'), " +
                        "comments TEXT, " +
                        "approval_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.execute(createApprovalHistoryTable);
                System.out.println("✅ Approval history table created/verified");

                // ===== INSERT DEFAULT DATA =====
                System.out.println("\n📊 Inserting default data...");
                
                // Insert departments
                try {
                    String insertDepartments = "INSERT IGNORE INTO departments (dept_code, dept_name, hod_name) VALUES " +
                            "('D01', 'IT', 'IT Head'), " +
                            "('D02', 'HR', 'HR Head'), " +
                            "('D03', 'Finance', 'Finance Head'), " +
                            "('D04', 'Operations', 'Operations Head'), " +
                            "('D05', 'Maintenance', 'Maintenance Head')";
                    stmt.executeUpdate(insertDepartments);
                    System.out.println("✅ Default departments inserted");
                } catch (SQLException e) {
                    System.out.println("⚠️ Departments already exist or error: " + e.getMessage());
                }

                // Insert employees with MD5 hashed passwords
                try {
                    String insertEmployees = "INSERT IGNORE INTO employees (emp_id, emp_name, designation, dept_code, dept_name, password_hash, role) VALUES " +
                            "('admin', 'System Administrator', 'Admin', 'D01', 'IT', MD5('admin123'), 'Admin'), " +
                            "('garage', 'Garage Manager', 'Garage Staff', 'D05', 'Maintenance', MD5('garage123'), 'Garage'), " +
                            "('hod01', 'IT Department Head', 'HOD', 'D01', 'IT', MD5('hod123'), 'HOD'), " +
                            "('coo01', 'Chief Operating Officer', 'COO', NULL, NULL, MD5('coo123'), 'COO'), " +
                            "('10101', 'Aarushi Jhawar', 'Engineer', 'D01', 'IT', MD5('aaru@123'), 'Employee'), " +
                            "('10202', 'Rohan Singh', 'Manager', 'D02', 'HR', MD5('rohan@123'), 'Employee'), " +
                            "('10303', 'Priya Sharma', 'Analyst', 'D03', 'Finance', MD5('priya@123'), 'Employee'), " +
                            "('10404', 'Amit Patel', 'Supervisor', 'D04', 'Operations', MD5('amit@123'), 'Employee')";
                    stmt.executeUpdate(insertEmployees);
                    System.out.println("✅ Default employees inserted");
                } catch (SQLException e) {
                    System.out.println("⚠️ Employees already exist or error: " + e.getMessage());
                }

                // Insert vehicles
                try {
                    String insertVehicles = "INSERT IGNORE INTO vehicles (vehicle_name, vehicle_number, vehicle_type, capacity, fuel_type, is_available) VALUES " +
                            "('Toyota Innova', 'MP23AB1234', 'SUV', 7, 'Diesel', TRUE), " +
                            "('Maruti Swift', 'MP23CD5678', 'Car', 4, 'Petrol', TRUE), " +
                            "('Tata Sumo', 'MP23EF9012', 'SUV', 9, 'Diesel', TRUE), " +
                            "('Mahindra Bolero', 'MP23GH3456', 'SUV', 8, 'Diesel', TRUE), " +
                            "('Eicher Bus', 'MP23IJ7890', 'Bus', 30, 'Diesel', TRUE)";
                    stmt.executeUpdate(insertVehicles);
                    System.out.println("✅ Default vehicles inserted");
                } catch (SQLException e) {
                    System.out.println("⚠️ Vehicles already exist or error: " + e.getMessage());
                }

                // Insert drivers
                try {
                    String insertDrivers = "INSERT IGNORE INTO drivers (driver_name, contact_number, license_number, license_expiry) VALUES " +
                            "('Rajesh Kumar', '9876543210', 'DL123456789012', '2025-12-31'), " +
                            "('Suresh Patel', '9876543211', 'DL123456789013', '2025-11-30'), " +
                            "('Mohan Singh', '9876543212', 'DL123456789014', '2025-10-31'), " +
                            "('Vikram Yadav', '9876543213', 'DL123456789015', '2025-09-30')";
                    stmt.executeUpdate(insertDrivers);
                    System.out.println("✅ Default drivers inserted");
                } catch (SQLException e) {
                    System.out.println("⚠️ Drivers already exist or error: " + e.getMessage());
                }

                System.out.println("\n🎉 Database setup completed successfully!");
                
            } catch (SQLException e) {
                System.err.println("❌ Error creating tables: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("1. MySQL is running (sudo service mysql start)");
            System.err.println("2. Database 'vehicle_requisition_db' exists");
            System.err.println("3. Username/password are correct in DatabaseConnection.java");
            System.err.println("4. MySQL JDBC driver is in classpath");
            e.printStackTrace();
        }
    }

    private static void createDatabaseIfNotExists() {
        String tempURL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
        
        try (Connection conn = DriverManager.getConnection(tempURL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Check if database exists
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'vehicle_requisition_db'");
            if (!rs.next()) {
                System.out.println("📁 Database doesn't exist, creating...");
                stmt.execute("CREATE DATABASE vehicle_requisition_db");
                System.out.println("✅ Database created successfully");
            } else {
                System.out.println("✅ Database already exists");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to create database: " + e.getMessage());
            System.err.println("Using default database instead...");
        }
    }

    public static void testConnection() {
        System.out.println("\n🔍 Testing Database Connection...");
        System.out.println("URL: " + URL);
        System.out.println("User: " + USER);
        System.out.println("Password: " + (PASSWORD.isEmpty() ? "[empty]" : "******"));
        
        try (Connection conn = getConnection()) {
            System.out.println("\n✅ Connection successful!");
            
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Product: " + meta.getDatabaseProductName());
            System.out.println("Database Version: " + meta.getDatabaseProductVersion());
            System.out.println("Driver Name: " + meta.getDriverName());
            System.out.println("Driver Version: " + meta.getDriverVersion());
            
            // Count records
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 'Departments' as table_name, COUNT(*) as count FROM departments " +
                                                "UNION SELECT 'Employees', COUNT(*) FROM employees " +
                                                "UNION SELECT 'Vehicles', COUNT(*) FROM vehicles " +
                                                "UNION SELECT 'Drivers', COUNT(*) FROM drivers");
                
                System.out.println("\n📊 Current data counts:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("table_name") + ": " + rs.getInt("count"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("\n❌ Connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\n💡 Troubleshooting steps:");
            System.err.println("1. Check if MySQL is running:");
            System.err.println("   Windows: Open Services and start MySQL");
            System.err.println("   Linux/Mac: sudo service mysql status");
            System.err.println("2. Verify credentials in DatabaseConnection.java");
            System.err.println("3. Try connecting manually:");
            System.err.println("   mysql -u " + USER + " -p");
            System.err.println("4. Create database:");
            System.err.println("   CREATE DATABASE vehicle_requisition_db;");
            System.err.println("5. Grant privileges:");
            System.err.println("   GRANT ALL ON vehicle_requisition_db.* TO '" + USER + "'@'localhost';");
        }
    }

    // ===== MAIN METHOD FOR TESTING =====
    public static void main(String[] args) {
        System.out.println("🚗 Vehicle Requisition Portal Database Setup");
        System.out.println("===========================================\n");
        
        testConnection();
        
        System.out.println("\n\n🛠️ Creating tables and inserting data...");
        createTables();
        
        System.out.println("\n\n✅ Setup complete!");
    }
}