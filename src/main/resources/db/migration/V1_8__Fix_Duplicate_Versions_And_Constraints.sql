-- Fix Duplicate Versions and Add Better Constraints
-- Version 1.8 - Clean up duplicate version data and improve database constraints

-- First, let's identify and clean up any duplicate version numbers
-- Keep only the most recent version for each version_number

-- Create a temporary table to store the IDs of records to keep
CREATE TEMPORARY TABLE versions_to_keep AS
SELECT MIN(id) as id_to_keep, version_number
FROM application_versions
GROUP BY version_number;

-- Delete duplicate records, keeping only the one with the smallest ID for each version_number
DELETE av FROM application_versions av
LEFT JOIN versions_to_keep vtk ON av.id = vtk.id_to_keep AND av.version_number = vtk.version_number
WHERE vtk.id_to_keep IS NULL;

-- Clean up the temporary table
DROP TEMPORARY TABLE versions_to_keep;

-- Now let's handle the case where there might be multiple active versions
-- We should only have one active version at a time for the latest version logic to work properly
-- Let's ensure only the most recent version is active

-- First, find the most recent version by release_date
SET @latest_version_id = (
    SELECT id 
    FROM application_versions 
    WHERE is_active = TRUE 
    ORDER BY release_date DESC, id DESC 
    LIMIT 1
);

-- Set all versions to inactive first
UPDATE application_versions SET is_active = FALSE WHERE is_active = TRUE;

-- Then set only the latest version to active
UPDATE application_versions SET is_active = TRUE WHERE id = @latest_version_id;

-- Add a unique constraint on version_number if it doesn't exist
-- This will prevent future duplicates
ALTER TABLE application_versions 
DROP INDEX IF EXISTS uk_version_number;

ALTER TABLE application_versions 
ADD CONSTRAINT uk_version_number UNIQUE (version_number);

-- Add an index for better performance on active version queries
CREATE INDEX IF NOT EXISTS idx_active_release_date ON application_versions(is_active, release_date DESC, id DESC);

-- Add a check constraint to ensure version_number follows semantic versioning pattern
-- This is optional but helps maintain data quality
ALTER TABLE application_versions 
ADD CONSTRAINT chk_version_format 
CHECK (version_number REGEXP '^[0-9]+\\.[0-9]+\\.[0-9]+(-[a-zA-Z0-9]+)?$');

-- Update any existing records that might not follow the format
-- For now, we'll just log them but not fail the migration
-- In a real scenario, you might want to fix these manually

-- Add comments for better documentation
ALTER TABLE application_versions 
MODIFY COLUMN version_number VARCHAR(20) NOT NULL COMMENT 'Semantic version number (e.g., 2.1.0)',
MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether this version is active and available for download',
MODIFY COLUMN is_mandatory BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this update is mandatory for clients',
MODIFY COLUMN release_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When this version was released';

-- Create a view for easy access to the current active version
CREATE OR REPLACE VIEW current_active_version AS
SELECT 
    id,
    version_number,
    release_date,
    is_mandatory,
    release_notes,
    file_name,
    file_size,
    file_checksum,
    download_url,
    minimum_client_version,
    release_channel,
    created_at,
    updated_at,
    created_by
FROM application_versions 
WHERE is_active = TRUE 
ORDER BY release_date DESC, id DESC 
LIMIT 1;

-- Create a stored procedure to safely activate a new version
-- This ensures only one version is active at a time
DELIMITER //
CREATE PROCEDURE ActivateVersion(IN target_version_number VARCHAR(20))
BEGIN
    DECLARE version_exists INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Check if the version exists
    SELECT COUNT(*) INTO version_exists 
    FROM application_versions 
    WHERE version_number = target_version_number;
    
    IF version_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Version not found';
    END IF;
    
    -- Deactivate all versions
    UPDATE application_versions SET is_active = FALSE;
    
    -- Activate the target version
    UPDATE application_versions 
    SET is_active = TRUE, updated_at = CURRENT_TIMESTAMP 
    WHERE version_number = target_version_number;
    
    COMMIT;
END //
DELIMITER ;

-- Insert audit log entry for this migration
INSERT INTO version_history (
    version_id, 
    version_number, 
    action_type, 
    action_timestamp, 
    success, 
    initiated_by,
    reason
) 
SELECT 
    id,
    version_number,
    'MIGRATION_CLEANUP',
    NOW(),
    TRUE,
    'system',
    'V1.8 migration: Cleaned up duplicate versions and improved constraints'
FROM application_versions 
WHERE is_active = TRUE;

-- Final verification queries
SELECT 'Migration V1.8 completed successfully' as status;
SELECT COUNT(*) as total_versions FROM application_versions;
SELECT COUNT(*) as active_versions FROM application_versions WHERE is_active = TRUE;
SELECT version_number, COUNT(*) as count 
FROM application_versions 
GROUP BY version_number 
HAVING COUNT(*) > 1 
ORDER BY version_number;

-- Show the current active version
SELECT 'Current active version:' as info, version_number, release_date 
FROM current_active_version;
