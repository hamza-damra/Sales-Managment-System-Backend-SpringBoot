-- Add soft delete fields to customers table
ALTER TABLE customers 
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by VARCHAR(255) NULL,
ADD COLUMN deletion_reason TEXT NULL;

-- Create index for better query performance on soft delete queries
CREATE INDEX idx_customers_is_deleted ON customers(is_deleted);
CREATE INDEX idx_customers_deleted_at ON customers(deleted_at);

-- Update existing customers to ensure they are not marked as deleted
UPDATE customers SET is_deleted = FALSE WHERE is_deleted IS NULL;
