-- MySQL Initialization Script for Sales Management System
-- This script sets up the database with proper permissions and optimizations

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS sales_management 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Use the database
USE sales_management;

-- Grant all privileges to the sales_user
GRANT ALL PRIVILEGES ON sales_management.* TO 'sales_user'@'%';

-- Create additional indexes for better performance (if tables exist)
-- Note: These will be created by Hibernate, but we can add custom indexes here

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Set some MySQL optimizations for the application
-- Note: innodb_log_file_size is read-only in MySQL 8.0 and must be set in my.cnf
SET GLOBAL innodb_buffer_pool_size = 134217728; -- 128MB
SET GLOBAL max_connections = 200;

-- Create a health check table for monitoring
CREATE TABLE IF NOT EXISTS health_check (
    id INT PRIMARY KEY AUTO_INCREMENT,
    status VARCHAR(10) DEFAULT 'OK',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial health check record
INSERT INTO health_check (status) VALUES ('OK') 
ON DUPLICATE KEY UPDATE status = 'OK', last_updated = CURRENT_TIMESTAMP;
