-- Migration script to fix product_additional_images table and foreign key constraints
-- Version: V1.9
-- Description: Ensure proper creation and constraints for product_additional_images table

-- First, check if the table exists and drop it if it has issues
DROP TABLE IF EXISTS product_additional_images;

-- Create the product_additional_images table with proper structure
-- This table is used by the @ElementCollection annotation in the Product entity
CREATE TABLE product_additional_images (
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    
    -- Create index for better performance
    INDEX idx_product_additional_images_product_id (product_id),
    INDEX idx_product_additional_images_image_url (image_url),
    
    -- Add foreign key constraint with proper naming
    CONSTRAINT fk_product_additional_images_product_id 
        FOREIGN KEY (product_id) REFERENCES products(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add table comment for documentation
ALTER TABLE product_additional_images 
COMMENT = 'Additional product images - supports multiple images per product via @ElementCollection';

-- Verify the table structure
SELECT 'Product additional images table created successfully' as status;

-- Show table structure for verification
DESCRIBE product_additional_images;

-- Check foreign key constraints
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'product_additional_images' 
AND CONSTRAINT_NAME LIKE 'fk_%';
