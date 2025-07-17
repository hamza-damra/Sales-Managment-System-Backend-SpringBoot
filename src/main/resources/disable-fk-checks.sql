-- Disable foreign key checks for MySQL schema creation
-- This allows tables to be created in any order without constraint failures

SET FOREIGN_KEY_CHECKS = 0;
SET sql_require_primary_key = 0;
SET sql_mode = 'TRADITIONAL';
SET autocommit = 1;

-- Additional MySQL settings for better schema creation compatibility
SET unique_checks = 0;
SET sql_notes = 0;

-- These settings will be applied before schema creation
-- and will allow Hibernate to create all tables without foreign key constraint errors
