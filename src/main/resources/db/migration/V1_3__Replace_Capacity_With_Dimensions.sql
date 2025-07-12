-- Migration script to replace capacity field with physical dimensions
-- Version: V1.3
-- Description: Remove capacity column and add length, width, height columns to inventories table

-- Add new dimension columns
ALTER TABLE inventories 
ADD COLUMN length DECIMAL(12,2) NULL COMMENT 'Length in meters',
ADD COLUMN width DECIMAL(12,2) NULL COMMENT 'Width in meters',
ADD COLUMN height DECIMAL(12,2) NULL COMMENT 'Height in meters';

-- Add constraints for dimensions (must be positive if not null)
ALTER TABLE inventories 
ADD CONSTRAINT chk_length_positive CHECK (length IS NULL OR length > 0),
ADD CONSTRAINT chk_width_positive CHECK (width IS NULL OR width > 0),
ADD CONSTRAINT chk_height_positive CHECK (height IS NULL OR height > 0);

-- Optional: Set some default dimensions for existing records based on capacity
-- This is a sample conversion - adjust the logic based on your business requirements
-- Assuming capacity represents number of items and we estimate space per item
UPDATE inventories 
SET 
    length = CASE 
        WHEN capacity IS NOT NULL AND capacity > 0 THEN 
            ROUND(SQRT(capacity * 2.0), 2)  -- Estimate length based on capacity
        ELSE NULL 
    END,
    width = CASE 
        WHEN capacity IS NOT NULL AND capacity > 0 THEN 
            ROUND(SQRT(capacity * 2.0), 2)  -- Estimate width based on capacity
        ELSE NULL 
    END,
    height = CASE 
        WHEN capacity IS NOT NULL AND capacity > 0 THEN 
            3.0  -- Default height of 3 meters
        ELSE NULL 
    END
WHERE capacity IS NOT NULL;

-- Remove the capacity column
ALTER TABLE inventories DROP COLUMN capacity;

-- Add indexes for better query performance on dimensions
CREATE INDEX idx_inventories_dimensions ON inventories(length, width, height);
CREATE INDEX idx_inventories_length ON inventories(length);
CREATE INDEX idx_inventories_width ON inventories(width);
CREATE INDEX idx_inventories_height ON inventories(height);

-- Add comments to document the changes
ALTER TABLE inventories 
MODIFY COLUMN length DECIMAL(12,2) NULL COMMENT 'Physical length of inventory space in meters',
MODIFY COLUMN width DECIMAL(12,2) NULL COMMENT 'Physical width of inventory space in meters',
MODIFY COLUMN height DECIMAL(12,2) NULL COMMENT 'Physical height of inventory space in meters';
