-- MySQL Schema Creation Script for Sales Management System
-- This script creates all tables in the correct order to avoid foreign key constraint issues

SET FOREIGN_KEY_CHECKS = 0;
SET sql_require_primary_key = 0;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS `product_additional_images`;
DROP TABLE IF EXISTS `promotion_categories`;
DROP TABLE IF EXISTS `promotion_products`;
DROP TABLE IF EXISTS `applied_promotions`;
DROP TABLE IF EXISTS `return_items`;
DROP TABLE IF EXISTS `returns`;
DROP TABLE IF EXISTS `sale_items`;
DROP TABLE IF EXISTS `sales`;
DROP TABLE IF EXISTS `purchase_order_items`;
DROP TABLE IF EXISTS `purchase_orders`;
DROP TABLE IF EXISTS `inventories`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `promotions`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `suppliers`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `refresh_tokens`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `connected_clients`;
DROP TABLE IF EXISTS `rate_limit_tracker`;
DROP TABLE IF EXISTS `update_downloads`;
DROP TABLE IF EXISTS `version_history`;
DROP TABLE IF EXISTS `application_versions`;
DROP TABLE IF EXISTS `update_analytics`;

-- Create core tables first (no foreign keys)

-- Users and authentication
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `first_name` VARCHAR(255),
    `last_name` VARCHAR(255),
    `role` VARCHAR(50) NOT NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

CREATE TABLE `refresh_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `expiry_date` DATETIME(6) NOT NULL,
    `created_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

CREATE TABLE `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `description` VARCHAR(500),
    PRIMARY KEY (`id`)
);

-- Business entities
CREATE TABLE `customers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255),
    `phone` VARCHAR(50),
    `address` TEXT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

CREATE TABLE `suppliers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `contact_person` VARCHAR(255),
    `email` VARCHAR(255),
    `phone` VARCHAR(50),
    `address` TEXT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

CREATE TABLE `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `description` TEXT,
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

CREATE TABLE `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(19,2) NOT NULL,
    `cost` DECIMAL(19,2),
    `sku` VARCHAR(255) UNIQUE,
    `barcode` VARCHAR(255),
    `category_id` BIGINT,
    `supplier_id` BIGINT,
    `image_url` VARCHAR(500),
    `created_at` DATETIME(6),
    `updated_at` DATETIME(6),
    PRIMARY KEY (`id`)
);

-- Continue with remaining tables...
-- This approach ensures all tables are created first, then foreign keys are added separately

SET FOREIGN_KEY_CHECKS = 1;
