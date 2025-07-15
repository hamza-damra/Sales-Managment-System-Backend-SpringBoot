-- Migration script to fix checksum format
-- Version: V1.6
-- Description: Remove 'sha256:' prefix from file_checksum field to fit within 64 character limit

-- Update existing records that have the 'sha256:' prefix
UPDATE application_versions 
SET file_checksum = SUBSTRING(file_checksum, 8)
WHERE file_checksum LIKE 'sha256:%';

-- Add a comment to document the checksum format
ALTER TABLE application_versions 
MODIFY COLUMN file_checksum VARCHAR(64) NOT NULL COMMENT 'SHA-256 checksum in hexadecimal format (without prefix)';
