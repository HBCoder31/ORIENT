-- setup_mysql.sql
-- Run this in MySQL: mysql -u root -p < setup_mysql.sql

-- Create database
CREATE DATABASE IF NOT EXISTS vehicle_requisition_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE vehicle_requisition_db;

-- Create departments table
CREATE TABLE IF NOT EXISTS departments (
    dept_code VARCHAR(10) PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL,
    hod_name VARCHAR(100),
    hod_email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX(dept_code)
);

-- Create employees table
CREATE TABLE IF NOT EXISTS employees (
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
    FOREIGN KEY (dept_code) REFERENCES departments(dept_code) ON DELETE SET NULL,
    INDEX(emp_id), INDEX(dept_code)
);

-- Insert default departments
INSERT IGNORE INTO departments (dept_code, dept_name, hod_name) VALUES 
    ('D01', 'IT', 'IT Head'),
    ('D02', 'HR', 'HR Head'),
    ('D03', 'Finance', 'Finance Head'),
    ('D04', 'Operations', 'Operations Head'),
    ('D05', 'Maintenance', 'Maintenance Head');

-- Insert default employees with MD5 hashed passwords
INSERT IGNORE INTO employees (emp_id, emp_name, designation, dept_code, dept_name, password_hash, role) VALUES 
    ('admin', 'System Administrator', 'Admin', 'D01', 'IT', MD5('admin123'), 'Admin'),
    ('garage', 'Garage Manager', 'Garage Staff', 'D05', 'Maintenance', MD5('garage123'), 'Garage'),
    ('hod01', 'IT Department Head', 'HOD', 'D01', 'IT', MD5('hod123'), 'HOD'),
    ('coo01', 'Chief Operating Officer', 'COO', NULL, NULL, MD5('coo123'), 'COO'),
    ('10101', 'Aarushi Jhawar', 'Engineer', 'D01', 'IT', MD5('aaru@123'), 'Employee'),
    ('10202', 'Rohan Singh', 'Manager', 'D02', 'HR', MD5('rohan@123'), 'Employee'),
    ('10303', 'Priya Sharma', 'Analyst', 'D03', 'Finance', MD5('priya@123'), 'Employee'),
    ('10404', 'Amit Patel', 'Supervisor', 'D04', 'Operations', MD5('amit@123'), 'Employee');

-- Show confirmation
SELECT 'Database vehicle_requisition_db setup completed!' as message;
SELECT COUNT(*) as department_count FROM departments;
SELECT COUNT(*) as employee_count FROM employees;