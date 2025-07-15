-- Migration for Backend Update System Tables
-- Version: V1.5
-- Description: Add tables for application version management, download tracking, and client connections

-- Create application_versions table
CREATE TABLE application_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_number VARCHAR(20) NOT NULL UNIQUE,
    release_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    release_notes TEXT,
    minimum_client_version VARCHAR(20),
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_checksum VARCHAR(64) NOT NULL,
    download_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    INDEX idx_version_number (version_number),
    INDEX idx_is_active (is_active),
    INDEX idx_release_date (release_date)
);

-- Create update_downloads table
CREATE TABLE update_downloads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    client_identifier VARCHAR(255),
    download_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_completed_at TIMESTAMP NULL,
    download_status ENUM('STARTED', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'STARTED',
    client_ip VARCHAR(45),
    user_agent TEXT,
    
    FOREIGN KEY (version_id) REFERENCES application_versions(id) ON DELETE CASCADE,
    INDEX idx_version_id (version_id),
    INDEX idx_download_status (download_status),
    INDEX idx_client_identifier (client_identifier)
);

-- Create connected_clients table
CREATE TABLE connected_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    client_version VARCHAR(20),
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_ping_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(45),
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_session_id (session_id),
    INDEX idx_client_version (client_version),
    INDEX idx_is_active (is_active)
);

-- Insert initial version data (current version 2.0.0)
INSERT INTO application_versions (
    version_number, 
    release_date, 
    is_mandatory, 
    is_active, 
    release_notes, 
    minimum_client_version, 
    file_name, 
    file_size, 
    file_checksum, 
    download_url, 
    created_by
) VALUES (
    '2.0.0',
    CURRENT_TIMESTAMP,
    FALSE,
    TRUE,
    'Initial version of the Sales Management System with comprehensive features including sales tracking, inventory management, customer management, and reporting capabilities.',
    '1.0.0',
    'sales-management-2.0.0.jar',
    0,
    'initial-version-placeholder',
    '/api/v1/updates/download/2.0.0',
    'system'
);
