-- Enhanced Update System Database Migration
-- Version 1.7 - Add support for release channels, version history, analytics, and rate limiting

-- Add release channel to application_versions table
ALTER TABLE application_versions 
ADD COLUMN release_channel VARCHAR(20) DEFAULT 'STABLE' AFTER created_by;

-- Create version_history table for rollback tracking
CREATE TABLE version_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    version_number VARCHAR(20) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_identifier VARCHAR(255),
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    previous_version VARCHAR(20),
    target_version VARCHAR(20),
    reason TEXT,
    success BOOLEAN,
    error_message TEXT,
    duration_seconds INT,
    initiated_by VARCHAR(100),
    
    FOREIGN KEY (version_id) REFERENCES application_versions(id) ON DELETE CASCADE,
    INDEX idx_version_number (version_number),
    INDEX idx_action_type (action_type),
    INDEX idx_action_timestamp (action_timestamp),
    INDEX idx_client_identifier (client_identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create update_analytics table for detailed analytics
CREATE TABLE update_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(30) NOT NULL,
    event_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_number VARCHAR(20),
    client_identifier VARCHAR(255),
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    release_channel VARCHAR(20),
    download_size_bytes BIGINT,
    download_duration_seconds INT,
    download_speed_mbps DOUBLE,
    success BOOLEAN,
    error_code VARCHAR(50),
    error_message TEXT,
    retry_count INT DEFAULT 0,
    resumed_download BOOLEAN DEFAULT FALSE,
    bytes_already_downloaded BIGINT,
    country_code VARCHAR(100),
    region VARCHAR(100),
    connection_type VARCHAR(50),
    is_delta_update BOOLEAN DEFAULT FALSE,
    delta_compression_ratio DOUBLE,
    metadata TEXT,
    
    INDEX idx_event_type (event_type),
    INDEX idx_event_timestamp (event_timestamp),
    INDEX idx_version_number (version_number),
    INDEX idx_client_identifier (client_identifier),
    INDEX idx_release_channel (release_channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create rate_limit_tracker table for rate limiting
CREATE TABLE rate_limit_tracker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_identifier VARCHAR(255) NOT NULL,
    client_ip VARCHAR(45),
    endpoint_type VARCHAR(30) NOT NULL,
    request_count INT NOT NULL DEFAULT 0,
    window_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_until DATETIME,
    total_blocked_requests BIGINT DEFAULT 0,
    total_allowed_requests BIGINT DEFAULT 0,
    first_violation_time DATETIME,
    violation_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_client_endpoint (client_identifier, endpoint_type),
    INDEX idx_client_identifier (client_identifier),
    INDEX idx_endpoint_type (endpoint_type),
    INDEX idx_window_start (window_start),
    INDEX idx_last_request (last_request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Update existing application_versions with default release channel
UPDATE application_versions 
SET release_channel = 'STABLE' 
WHERE release_channel IS NULL;

-- Create indexes for better performance on existing tables
CREATE INDEX idx_application_versions_release_channel ON application_versions(release_channel);
CREATE INDEX idx_application_versions_active_mandatory ON application_versions(is_active, is_mandatory);
CREATE INDEX idx_update_downloads_status_timestamp ON update_downloads(download_status, download_timestamp);

-- Insert sample data for testing (optional - remove in production)
-- This adds some sample version history entries
INSERT INTO version_history (version_id, version_number, action_type, client_identifier, client_ip, success, duration_seconds, initiated_by)
SELECT 
    id,
    version_number,
    'INSTALL',
    CONCAT('client-', id),
    '192.168.1.100',
    TRUE,
    FLOOR(RAND() * 300) + 30,
    'system'
FROM application_versions 
WHERE id <= 5;

-- Insert sample analytics data
INSERT INTO update_analytics (event_type, version_number, client_identifier, client_ip, release_channel, success, download_size_bytes, download_duration_seconds)
SELECT 
    'DOWNLOAD_COMPLETED',
    version_number,
    CONCAT('client-', id),
    '192.168.1.100',
    COALESCE(release_channel, 'STABLE'),
    TRUE,
    file_size,
    FLOOR(RAND() * 600) + 60
FROM application_versions 
WHERE id <= 3;

-- Create a view for update statistics (useful for reporting)
CREATE VIEW update_statistics_view AS
SELECT 
    av.version_number,
    av.release_channel,
    av.release_date,
    COUNT(DISTINCT ua.client_identifier) as unique_downloads,
    COUNT(ua.id) as total_download_attempts,
    SUM(CASE WHEN ua.success = TRUE THEN 1 ELSE 0 END) as successful_downloads,
    SUM(CASE WHEN ua.success = FALSE THEN 1 ELSE 0 END) as failed_downloads,
    ROUND(AVG(ua.download_duration_seconds), 2) as avg_download_time_seconds,
    ROUND(AVG(ua.download_speed_mbps), 2) as avg_download_speed_mbps,
    COUNT(DISTINCT vh.client_identifier) as unique_installations,
    SUM(CASE WHEN vh.action_type = 'ROLLBACK' THEN 1 ELSE 0 END) as rollback_count
FROM application_versions av
LEFT JOIN update_analytics ua ON av.version_number = ua.version_number 
    AND ua.event_type = 'DOWNLOAD_COMPLETED'
LEFT JOIN version_history vh ON av.version_number = vh.version_number 
    AND vh.action_type IN ('UPDATE', 'INSTALL', 'ROLLBACK')
GROUP BY av.id, av.version_number, av.release_channel, av.release_date
ORDER BY av.release_date DESC;

-- Create a view for rate limiting statistics
CREATE VIEW rate_limit_statistics_view AS
SELECT 
    endpoint_type,
    COUNT(DISTINCT client_identifier) as unique_clients,
    SUM(total_allowed_requests) as total_allowed,
    SUM(total_blocked_requests) as total_blocked,
    ROUND(
        CASE 
            WHEN SUM(total_allowed_requests + total_blocked_requests) > 0 
            THEN (SUM(total_blocked_requests) * 100.0) / SUM(total_allowed_requests + total_blocked_requests)
            ELSE 0 
        END, 2
    ) as block_rate_percentage,
    COUNT(CASE WHEN blocked_until IS NOT NULL AND blocked_until > NOW() THEN 1 END) as currently_blocked,
    AVG(violation_count) as avg_violations_per_client
FROM rate_limit_tracker
GROUP BY endpoint_type
ORDER BY total_allowed DESC;

-- Add comments to tables for documentation
ALTER TABLE version_history COMMENT = 'Tracks all version-related actions including updates, rollbacks, and installations for audit purposes';
ALTER TABLE update_analytics COMMENT = 'Stores detailed analytics data for update system monitoring and reporting';
ALTER TABLE rate_limit_tracker COMMENT = 'Manages per-client rate limiting across different endpoint types';

-- Add column comments for better documentation
ALTER TABLE version_history 
MODIFY COLUMN action_type VARCHAR(20) NOT NULL COMMENT 'Type of action: UPDATE, ROLLBACK, INSTALL, UNINSTALL, REPAIR, VERIFY',
MODIFY COLUMN success BOOLEAN COMMENT 'Whether the action completed successfully',
MODIFY COLUMN duration_seconds INT COMMENT 'Time taken to complete the action in seconds';

ALTER TABLE update_analytics 
MODIFY COLUMN event_type VARCHAR(30) NOT NULL COMMENT 'Type of event: DOWNLOAD_STARTED, DOWNLOAD_COMPLETED, UPDATE_APPLIED, etc.',
MODIFY COLUMN retry_count INT DEFAULT 0 COMMENT 'Number of retry attempts for this operation',
MODIFY COLUMN is_delta_update BOOLEAN DEFAULT FALSE COMMENT 'Whether this was a differential update';

ALTER TABLE rate_limit_tracker 
MODIFY COLUMN endpoint_type VARCHAR(30) NOT NULL COMMENT 'Type of endpoint: DOWNLOAD, METADATA, COMPATIBILITY, etc.',
MODIFY COLUMN violation_count INT DEFAULT 0 COMMENT 'Number of rate limit violations for this client';

-- Create stored procedure for cleanup old records (optional)
DELIMITER //
CREATE PROCEDURE CleanupOldUpdateRecords(IN days_to_keep INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Clean up old analytics records
    DELETE FROM update_analytics 
    WHERE event_timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    -- Clean up old version history (keep important actions longer)
    DELETE FROM version_history 
    WHERE action_timestamp < DATE_SUB(NOW(), INTERVAL days_to_keep DAY)
    AND action_type NOT IN ('ROLLBACK', 'INSTALL');
    
    -- Clean up inactive rate limit trackers
    DELETE FROM rate_limit_tracker 
    WHERE last_request_time < DATE_SUB(NOW(), INTERVAL 7 DAY)
    AND blocked_until IS NULL;
    
    COMMIT;
END //
DELIMITER ;

-- Grant necessary permissions (adjust as needed for your setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON update_statistics_view TO 'app_user'@'%';
-- GRANT SELECT ON rate_limit_statistics_view TO 'app_user'@'%';
-- GRANT EXECUTE ON PROCEDURE CleanupOldUpdateRecords TO 'app_admin'@'%';

-- Final verification queries (these will be logged but not affect the migration)
SELECT 'Enhanced Update System migration completed successfully' as status;
SELECT COUNT(*) as version_history_count FROM version_history;
SELECT COUNT(*) as update_analytics_count FROM update_analytics;
SELECT COUNT(*) as rate_limit_tracker_count FROM rate_limit_tracker;
SELECT COUNT(*) as application_versions_with_channel FROM application_versions WHERE release_channel IS NOT NULL;
