-- Migration script to replace operating_hours field with structured work times
-- Version: V1.4
-- Description: Remove operating_hours column and add start_work_time, end_work_time columns to inventories table

-- Add new work time columns
ALTER TABLE inventories 
ADD COLUMN start_work_time TIME NULL COMMENT 'Daily opening/start time',
ADD COLUMN end_work_time TIME NULL COMMENT 'Daily closing/end time';

-- Optional: Parse existing operating_hours data and convert to structured times
-- This is a sample conversion - adjust the logic based on your data format
-- Assuming operating_hours format is like "09:00-17:00" or "9 AM - 5 PM"

-- Update records with common operating hours patterns
UPDATE inventories 
SET 
    start_work_time = CASE 
        WHEN operating_hours LIKE '%9%-%17%' OR operating_hours LIKE '%09:00%17:00%' THEN '09:00:00'
        WHEN operating_hours LIKE '%8%-%16%' OR operating_hours LIKE '%08:00%16:00%' THEN '08:00:00'
        WHEN operating_hours LIKE '%7%-%15%' OR operating_hours LIKE '%07:00%15:00%' THEN '07:00:00'
        WHEN operating_hours LIKE '%10%-%18%' OR operating_hours LIKE '%10:00%18:00%' THEN '10:00:00'
        WHEN operating_hours LIKE '%9 AM%5 PM%' OR operating_hours LIKE '%9AM%5PM%' THEN '09:00:00'
        WHEN operating_hours LIKE '%8 AM%4 PM%' OR operating_hours LIKE '%8AM%4PM%' THEN '08:00:00'
        WHEN operating_hours LIKE '%24%' OR operating_hours LIKE '%24/7%' THEN '00:00:00'
        ELSE NULL
    END,
    end_work_time = CASE 
        WHEN operating_hours LIKE '%9%-%17%' OR operating_hours LIKE '%09:00%17:00%' THEN '17:00:00'
        WHEN operating_hours LIKE '%8%-%16%' OR operating_hours LIKE '%08:00%16:00%' THEN '16:00:00'
        WHEN operating_hours LIKE '%7%-%15%' OR operating_hours LIKE '%07:00%15:00%' THEN '15:00:00'
        WHEN operating_hours LIKE '%10%-%18%' OR operating_hours LIKE '%10:00%18:00%' THEN '18:00:00'
        WHEN operating_hours LIKE '%9 AM%5 PM%' OR operating_hours LIKE '%9AM%5PM%' THEN '17:00:00'
        WHEN operating_hours LIKE '%8 AM%4 PM%' OR operating_hours LIKE '%8AM%4PM%' THEN '16:00:00'
        WHEN operating_hours LIKE '%24%' OR operating_hours LIKE '%24/7%' THEN '23:59:59'
        ELSE NULL
    END
WHERE operating_hours IS NOT NULL;

-- Set default business hours for records without operating_hours
UPDATE inventories 
SET 
    start_work_time = '09:00:00',
    end_work_time = '17:00:00'
WHERE operating_hours IS NULL OR operating_hours = '';

-- Add constraint to ensure start_work_time is before end_work_time when both are set
ALTER TABLE inventories 
ADD CONSTRAINT chk_work_time_order 
CHECK (
    (start_work_time IS NULL OR end_work_time IS NULL) OR 
    (start_work_time < end_work_time)
);

-- Remove the operating_hours column
ALTER TABLE inventories DROP COLUMN operating_hours;

-- Add indexes for better query performance on work times
CREATE INDEX idx_inventories_start_work_time ON inventories(start_work_time);
CREATE INDEX idx_inventories_end_work_time ON inventories(end_work_time);
CREATE INDEX idx_inventories_work_times ON inventories(start_work_time, end_work_time);

-- Add comments to document the changes
ALTER TABLE inventories 
MODIFY COLUMN start_work_time TIME NULL COMMENT 'Daily opening/start time (24-hour format)',
MODIFY COLUMN end_work_time TIME NULL COMMENT 'Daily closing/end time (24-hour format)';
