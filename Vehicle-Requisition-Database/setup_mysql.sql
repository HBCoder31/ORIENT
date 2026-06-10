-- Drop the old database completely to start fresh
DROP DATABASE IF EXISTS vehicle_requisition_db;
CREATE DATABASE vehicle_requisition_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vehicle_requisition_db;

-- 1. Departments Table
CREATE TABLE departments (
    dept_code VARCHAR(10) PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL,
    hod_name VARCHAR(100),
    hod_email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Employees Table
CREATE TABLE employees (
    emp_id VARCHAR(10) PRIMARY KEY,
    emp_name VARCHAR(100) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    dept_code VARCHAR(10),
    dept_name VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    contact_number VARCHAR(15),
    email VARCHAR(100),
    role ENUM('Employee', 'Admin', 'Garage', 'HOD', 'COO') DEFAULT 'Employee',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_code) REFERENCES departments(dept_code) ON DELETE SET NULL
);

-- 3. Vehicles Table
CREATE TABLE vehicles (
    vehicle_number VARCHAR(50) PRIMARY KEY,
    vehicle_name VARCHAR(100),
    is_available BOOLEAN DEFAULT TRUE
);

-- 4. Vehicle Requests Table (This is the main one)
CREATE TABLE vehicle_requests (
    request_id VARCHAR(50) PRIMARY KEY, 
    emp_id VARCHAR(10) NOT NULL,
    
    -- Travel Details
    from_location VARCHAR(255),
    to_location VARCHAR(255),
    purpose TEXT,
    nature_of_work VARCHAR(50),
    beyond_area VARCHAR(10),
    beyond_location VARCHAR(255),
    travelers_type VARCHAR(50),
    family_count INT DEFAULT 1,
    train_details VARCHAR(255),
    contact_number VARCHAR(15),
    
    -- Status & Approvals
    status VARCHAR(50) DEFAULT 'Pending',
    hod_approval_status VARCHAR(50) DEFAULT 'Pending',
    coo_approval_status VARCHAR(50) DEFAULT 'Not Required',
    hod_approval_date DATETIME,
    coo_approval_date DATETIME,
    hod_approver_id VARCHAR(10),
    coo_approver_id VARCHAR(10),
    
    -- Garage & Vehicle Details
    vehicle_name VARCHAR(100),
    vehicle_number VARCHAR(50),
    driver_name VARCHAR(100),
    driver_contact VARCHAR(15),
    pickup_datetime DATETIME,
    dropoff_datetime DATETIME,
    trip_completed BOOLEAN DEFAULT FALSE,
    
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE
);

-- 5. Vehicle Assignments Table
CREATE TABLE vehicle_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50),
    vehicle_name VARCHAR(100),
    vehicle_number VARCHAR(50),
    driver_name VARCHAR(100),
    driver_contact VARCHAR(15),
    pickup_datetime DATETIME,
    dropoff_datetime DATETIME,
    trip_completed BOOLEAN DEFAULT FALSE,
    assigned_by VARCHAR(10),
    assignment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_date DATETIME,
    FOREIGN KEY (request_id) REFERENCES vehicle_requests(request_id) ON DELETE CASCADE
);

-- 6. Approval History Table
CREATE TABLE approval_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50),
    approver_id VARCHAR(10),
    approver_role VARCHAR(50),
    new_status VARCHAR(50),
    approval_decision VARCHAR(50),
    comments TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES vehicle_requests(request_id) ON DELETE CASCADE
);

-- 7. Audit Log Table
CREATE TABLE audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    actor_empid VARCHAR(10),
    actor_role VARCHAR(50),
    action_type VARCHAR(100),
    entity_type VARCHAR(100),
    entity_id VARCHAR(50),
    description TEXT,
    extra_data TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert defaults
INSERT IGNORE INTO departments (dept_code, dept_name, hod_name) VALUES 
    ('D01', 'IT', 'IT Head'), ('D02', 'HR', 'HR Head'), ('D03', 'Finance', 'Finance Head'), 
    ('D04', 'Operations', 'Operations Head'), ('D05', 'Maintenance', 'Maintenance Head');

INSERT IGNORE INTO employees (emp_id, emp_name, designation, dept_code, dept_name, password_hash, role) VALUES 
    ('admin', 'System Administrator', 'Admin', 'D01', 'IT', MD5('admin123'), 'Admin'),
    ('garage', 'Garage Manager', 'Garage Staff', 'D05', 'Maintenance', MD5('garage123'), 'Garage'),
    ('hod01', 'IT Department Head', 'HOD', 'D01', 'IT', MD5('hod123'), 'HOD'),
    ('coo01', 'Chief Operating Officer', 'COO', NULL, NULL, MD5('coo123'), 'COO'),
    ('10101', 'Akshat Nama', 'Engineer', 'D01', 'IT', MD5('aaru@123'), 'Employee');
