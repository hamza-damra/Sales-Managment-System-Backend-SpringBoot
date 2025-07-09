-- Migration script to add inventory tables and establish relationship with categories
-- Version: V1.2
-- Description: Add inventories table and modify categories table to include inventory_id foreign key

-- Create inventories table
CREATE TABLE inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    address TEXT,
    manager_name VARCHAR(255),
    manager_phone VARCHAR(20),
    manager_email VARCHAR(255),
    capacity INT,
    current_stock_count INT DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED', 'MAINTENANCE') DEFAULT 'ACTIVE',
    warehouse_code VARCHAR(100) UNIQUE,
    is_main_warehouse BOOLEAN DEFAULT FALSE,
    operating_hours VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_inventories_name (name),
    INDEX idx_inventories_status (status),
    INDEX idx_inventories_warehouse_code (warehouse_code),
    INDEX idx_inventories_is_main_warehouse (is_main_warehouse)
);

-- Add inventory_id foreign key column to categories table
ALTER TABLE categories 
ADD COLUMN inventory_id BIGINT,
ADD INDEX idx_categories_inventory_id (inventory_id),
ADD CONSTRAINT fk_categories_inventory 
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- Insert a default main warehouse for existing categories (optional)
INSERT INTO inventories (
    name, 
    description, 
    location, 
    status, 
    is_main_warehouse,
    current_stock_count,
    created_at,
    updated_at
) VALUES (
    'Main Warehouse',
    'Default main warehouse for existing categories',
    'Main Location',
    'ACTIVE',
    TRUE,
    0,
    NOW(),
    NOW()
);

-- Optional: Assign all existing categories to the main warehouse
-- Uncomment the following lines if you want to assign existing categories to the main warehouse
-- UPDATE categories 
-- SET inventory_id = (SELECT id FROM inventories WHERE name = 'Main Warehouse' LIMIT 1)
-- WHERE inventory_id IS NULL;

-- Add some sample inventories (optional - remove if not needed)
INSERT INTO inventories (
    name, 
    description, 
    location, 
    address,
    manager_name,
    manager_phone,
    status, 
    is_main_warehouse,
    warehouse_code,
    capacity,
    current_stock_count,
    created_at,
    updated_at
) VALUES 
(
    'North Warehouse',
    'Northern distribution center',
    'North Industrial Zone',
    '123 North Industrial Street, Industrial Zone, City',
    'Ahmed Hassan',
    '+1234567890',
    'ACTIVE',
    FALSE,
    'NW001',
    5000,
    0,
    NOW(),
    NOW()
),
(
    'South Warehouse',
    'Southern distribution center',
    'South Industrial Zone',
    '456 South Industrial Avenue, Industrial Zone, City',
    'Fatima Ali',
    '+1234567891',
    'ACTIVE',
    FALSE,
    'SW001',
    3000,
    0,
    NOW(),
    NOW()
),
(
    'East Warehouse',
    'Eastern distribution center',
    'East Industrial Zone',
    '789 East Industrial Road, Industrial Zone, City',
    'Omar Mahmoud',
    '+1234567892',
    'ACTIVE',
    FALSE,
    'EW001',
    4000,
    0,
    NOW(),
    NOW()
);

-- Add comments to tables for documentation
ALTER TABLE inventories COMMENT = 'Inventory/Warehouse management table - stores information about different warehouses and storage locations';
ALTER TABLE categories COMMENT = 'Product categories table - now includes relationship to inventories for warehouse-specific categorization';
