package com.hamza.salesmanagementbackend.config;

/**
 * Application-wide constants for API paths and URLs
 * This class centralizes all API endpoint paths to improve maintainability
 * and provide a single source of truth for URL configuration.
 */
public final class ApplicationConstants {

    // Private constructor to prevent instantiation
    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== BASE URL CONFIGURATION ====================

    /**
     * Base domain for the application
     * Change this to update the domain across the entire application
     */
    public static final String BASE_DOMAIN = "localhost";

    /**
     * Default port for the application
     */
    public static final String DEFAULT_PORT = "8081";

    /**
     * Complete base URL for the application
     */
    public static final String BASE_URL = "http://" + BASE_DOMAIN + ":" + DEFAULT_PORT;

    /**
     * MySQL port for external access
     */
    public static final String MYSQL_PORT = "3307";

    /**
     * phpMyAdmin port
     */
    public static final String PHPMYADMIN_PORT = "8080";

    /**
     * Complete phpMyAdmin URL
     */
    public static final String PHPMYADMIN_URL = "http://" + BASE_DOMAIN + ":" + PHPMYADMIN_PORT;

    // ==================== DATABASE CONFIGURATION ====================

    /**
     * Database Configuration
     * Values loaded from environment variables for security
     */
    public static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "localhost");

    /**
     * Database port
     */
    public static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");

    /**
     * Database name
     */
    public static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "sales_management");

    /**
     * Database username
     */
    public static final String DB_USERNAME = System.getenv().getOrDefault("DB_USERNAME", "sales_user");

    /**
     * Database password
     */
    public static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "password");

    /**
     * Primary database URL with SSL
     */
    public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl-mode=REQUIRED";

    /**
     * Replica database host
     */
    public static final String DB_REPLICA_HOST = System.getenv().getOrDefault("DB_REPLICA_HOST", "replica-" + DB_HOST);

    /**
     * Replica database URL with SSL
     */
    public static final String DB_REPLICA_URL = "jdbc:mysql://" + DB_REPLICA_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl-mode=REQUIRED";

    /**
     * Complete MySQL service URI (constructed from environment variables)
     */
    public static final String DB_SERVICE_URI = "mysql://" + DB_USERNAME + ":" + DB_PASSWORD + "@" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl-mode=REQUIRED";

    /**
     * Complete MySQL replica URI (constructed from environment variables)
     */
    public static final String DB_REPLICA_SERVICE_URI = "mysql://" + DB_USERNAME + ":" + DB_PASSWORD + "@" + DB_REPLICA_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl-mode=REQUIRED";

    // ==================== BASE API CONFIGURATION ====================

    /**
     * Base API path for all endpoints
     */
    public static final String API_BASE = "/api";

    /**
     * API version 1 path
     */
    public static final String API_V1 = "/v1";

    /**
     * Complete API v1 base path
     */
    public static final String API_V1_BASE = API_BASE + API_V1;

    // ==================== CONTROLLER BASE PATHS ====================
    
    /**
     * Authentication controller base path
     */
    public static final String AUTH_BASE = "/auth";
    
    /**
     * Customers controller base path
     */
    public static final String CUSTOMERS_BASE = "/customers";
    
    /**
     * Products controller base path
     */
    public static final String PRODUCTS_BASE = "/products";
    
    /**
     * Sales controller base path
     */
    public static final String SALES_BASE = "/sales";
    
    /**
     * Suppliers controller base path
     */
    public static final String SUPPLIERS_BASE = "/suppliers";
    
    /**
     * Categories controller base path
     */
    public static final String CATEGORIES_BASE = "/categories";
    
    /**
     * Inventories controller base path
     */
    public static final String INVENTORIES_BASE = "/inventories";
    
    /**
     * Promotions controller base path
     */
    public static final String PROMOTIONS_BASE = "/promotions";
    
    /**
     * Purchase orders controller base path
     */
    public static final String PURCHASE_ORDERS_BASE = "/purchase-orders";
    
    /**
     * Returns controller base path
     */
    public static final String RETURNS_BASE = "/returns";
    
    /**
     * Test data controller base path
     */
    public static final String TEST_DATA_BASE = "/test-data";
    
    /**
     * Reports controller base path (legacy)
     */
    public static final String REPORTS_BASE = "/reports";

    /**
     * Updates controller base path
     */
    public static final String UPDATES_BASE = "/updates";

    /**
     * Admin controller base path
     */
    public static final String ADMIN_BASE = "/admin";

    // ==================== COMPLETE API PATHS ====================
    
    /**
     * Complete authentication API path
     */
    public static final String API_AUTH = API_BASE + AUTH_BASE;
    
    /**
     * Complete customers API path
     */
    public static final String API_CUSTOMERS = API_BASE + CUSTOMERS_BASE;
    
    /**
     * Complete products API path
     */
    public static final String API_PRODUCTS = API_BASE + PRODUCTS_BASE;
    
    /**
     * Complete sales API path
     */
    public static final String API_SALES = API_BASE + SALES_BASE;
    
    /**
     * Complete suppliers API path
     */
    public static final String API_SUPPLIERS = API_BASE + SUPPLIERS_BASE;
    
    /**
     * Complete categories API path
     */
    public static final String API_CATEGORIES = API_BASE + CATEGORIES_BASE;
    
    /**
     * Complete inventories API path
     */
    public static final String API_INVENTORIES = API_BASE + INVENTORIES_BASE;
    
    /**
     * Complete promotions API path
     */
    public static final String API_PROMOTIONS = API_BASE + PROMOTIONS_BASE;
    
    /**
     * Complete purchase orders API path
     */
    public static final String API_PURCHASE_ORDERS = API_BASE + PURCHASE_ORDERS_BASE;
    
    /**
     * Complete returns API path
     */
    public static final String API_RETURNS = API_BASE + RETURNS_BASE;
    
    /**
     * Complete test data API path
     */
    public static final String API_TEST_DATA = API_BASE + TEST_DATA_BASE;
    
    /**
     * Complete reports API path (legacy)
     */
    public static final String API_REPORTS = API_BASE + REPORTS_BASE;
    
    /**
     * Complete reports API v1 path
     */
    public static final String API_V1_REPORTS = API_V1_BASE + REPORTS_BASE;

    /**
     * Complete updates API v1 path
     */
    public static final String API_V1_UPDATES = API_V1_BASE + UPDATES_BASE;

    /**
     * Complete admin updates API v1 path
     */
    public static final String API_V1_ADMIN_UPDATES = API_V1_BASE + ADMIN_BASE + UPDATES_BASE;

    // ==================== SECURITY PATHS ====================
    
    /**
     * Authentication wildcard path for security configuration
     */
    public static final String API_AUTH_WILDCARD = API_AUTH + "/**";
    
    /**
     * WebSocket wildcard path for security configuration
     */
    public static final String WS_WILDCARD = "/ws/**";

    /**
     * Updates wildcard path for security configuration
     */
    public static final String API_V1_UPDATES_WILDCARD = API_V1_UPDATES + "/**";

    /**
     * Admin updates wildcard path for security configuration
     */
    public static final String API_V1_ADMIN_UPDATES_WILDCARD = API_V1_ADMIN_UPDATES + "/**";
    
    /**
     * H2 console path for security configuration
     */
    public static final String H2_CONSOLE_WILDCARD = "/h2-console/**";
    
    /**
     * Swagger API docs path
     */
    public static final String SWAGGER_API_DOCS_WILDCARD = "/v3/api-docs/**";
    
    /**
     * Swagger UI path
     */
    public static final String SWAGGER_UI_WILDCARD = "/swagger-ui/**";
    
    /**
     * Swagger UI HTML path
     */
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";

    // ==================== COMMON ENDPOINT PATTERNS ====================

    /**
     * Search endpoint pattern
     */
    public static final String SEARCH_ENDPOINT = "/search";

    /**
     * Debug endpoint pattern
     */
    public static final String DEBUG_ENDPOINT = "/debug";

    /**
     * Test endpoint pattern
     */
    public static final String TEST_ENDPOINT = "/test";

    /**
     * Login endpoint pattern
     */
    public static final String LOGIN_ENDPOINT = "/login";

    /**
     * Signup endpoint pattern
     */
    public static final String SIGNUP_ENDPOINT = "/signup";

    /**
     * Refresh endpoint pattern
     */
    public static final String REFRESH_ENDPOINT = "/refresh";

    /**
     * Active endpoint pattern
     */
    public static final String ACTIVE_ENDPOINT = "/active";

    /**
     * Info endpoint pattern
     */
    public static final String INFO_ENDPOINT = "/info";

    /**
     * Empty endpoint pattern
     */
    public static final String EMPTY_ENDPOINT = "/empty";

    /**
     * Status endpoint pattern
     */
    public static final String STATUS_ENDPOINT = "/status";

    /**
     * Name endpoint pattern
     */
    public static final String NAME_ENDPOINT = "/name";

    /**
     * Inventory endpoint pattern
     */
    public static final String INVENTORY_ENDPOINT = "/inventory";

    /**
     * No inventory endpoint pattern
     */
    public static final String NO_INVENTORY_ENDPOINT = "/no-inventory";

    /**
     * Complete endpoint pattern
     */
    public static final String COMPLETE_ENDPOINT = "/complete";

    /**
     * Cancel endpoint pattern
     */
    public static final String CANCEL_ENDPOINT = "/cancel";

    /**
     * Stock endpoint pattern
     */
    public static final String STOCK_ENDPOINT = "/stock";

    /**
     * Reduce endpoint pattern
     */
    public static final String REDUCE_ENDPOINT = "/reduce";

    /**
     * KPI endpoint pattern
     */
    public static final String KPI_ENDPOINT = "/kpi";

    /**
     * Deactivate endpoint pattern
     */
    public static final String DEACTIVATE_ENDPOINT = "/deactivate";

    /**
     * Product endpoint pattern
     */
    public static final String PRODUCT_ENDPOINT = "/product";

    /**
     * Latest endpoint pattern
     */
    public static final String LATEST_ENDPOINT = "/latest";

    /**
     * Check endpoint pattern
     */
    public static final String CHECK_ENDPOINT = "/check";

    /**
     * Download endpoint pattern
     */
    public static final String DOWNLOAD_ENDPOINT = "/download";

    /**
     * Versions endpoint pattern
     */
    public static final String VERSIONS_ENDPOINT = "/versions";

    /**
     * Statistics endpoint pattern
     */
    public static final String STATISTICS_ENDPOINT = "/statistics";

    /**
     * Metadata endpoint pattern
     */
    public static final String METADATA_ENDPOINT = "/metadata";

    /**
     * Compatibility endpoint pattern
     */
    public static final String COMPATIBILITY_ENDPOINT = "/compatibility";

    /**
     * Rollback endpoint pattern
     */
    public static final String ROLLBACK_ENDPOINT = "/rollback";

    /**
     * Delta endpoint pattern
     */
    public static final String DELTA_ENDPOINT = "/delta";

    /**
     * Analytics endpoint pattern
     */
    public static final String ANALYTICS_ENDPOINT = "/analytics";

    /**
     * Channels endpoint pattern
     */
    public static final String CHANNELS_ENDPOINT = "/channels";

    /**
     * WebSocket updates endpoint
     */
    public static final String WS_UPDATES_ENDPOINT = "/ws/updates";

    // ==================== SPECIFIC ENDPOINT PATHS ====================
    
    /**
     * Authentication test endpoint
     */
    public static final String AUTH_TEST_ENDPOINT = API_AUTH + TEST_ENDPOINT;
    
    /**
     * Authentication login endpoint
     */
    public static final String AUTH_LOGIN_ENDPOINT = API_AUTH + "/login";
    
    /**
     * Authentication signup endpoint
     */
    public static final String AUTH_SIGNUP_ENDPOINT = API_AUTH + "/signup";
    
    /**
     * Authentication refresh endpoint
     */
    public static final String AUTH_REFRESH_ENDPOINT = API_AUTH + "/refresh";

    // ==================== REPORT SPECIFIC PATHS ====================
    
    /**
     * Dashboard endpoint
     */
    public static final String DASHBOARD_ENDPOINT = "/dashboard";
    
    /**
     * Executive dashboard endpoint
     */
    public static final String EXECUTIVE_DASHBOARD_ENDPOINT = DASHBOARD_ENDPOINT + "/executive";
    
    /**
     * Operational dashboard endpoint
     */
    public static final String OPERATIONAL_DASHBOARD_ENDPOINT = DASHBOARD_ENDPOINT + "/operational";
    
    /**
     * Real-time KPIs endpoint
     */
    public static final String REAL_TIME_KPI_ENDPOINT = "/kpi/real-time";
    
    /**
     * Complete dashboard API v1 path
     */
    public static final String API_V1_DASHBOARD = API_V1_REPORTS + DASHBOARD_ENDPOINT;
    
    /**
     * Complete executive dashboard API v1 path
     */
    public static final String API_V1_EXECUTIVE_DASHBOARD = API_V1_REPORTS + EXECUTIVE_DASHBOARD_ENDPOINT;
    
    /**
     * Complete operational dashboard API v1 path
     */
    public static final String API_V1_OPERATIONAL_DASHBOARD = API_V1_REPORTS + OPERATIONAL_DASHBOARD_ENDPOINT;
    
    /**
     * Complete real-time KPIs API v1 path
     */
    public static final String API_V1_REAL_TIME_KPI = API_V1_REPORTS + REAL_TIME_KPI_ENDPOINT;

    // ==================== DEBUG SPECIFIC PATHS ====================

    /**
     * Debug all endpoint
     */
    public static final String DEBUG_ALL_ENDPOINT = DEBUG_ENDPOINT + "/all";

    /**
     * Debug fix null deleted endpoint
     */
    public static final String DEBUG_FIX_NULL_DELETED_ENDPOINT = DEBUG_ENDPOINT + "/fix-null-deleted";

    // ==================== COMPOSITE ENDPOINT PATHS ====================

    /**
     * Stock reduce endpoint
     */
    public static final String STOCK_REDUCE_ENDPOINT = STOCK_ENDPOINT + REDUCE_ENDPOINT;

    /**
     * KPI real-time endpoint (for legacy reports)
     */
    public static final String KPI_REAL_TIME_ENDPOINT_LEGACY = KPI_ENDPOINT + "/real-time";

    // ==================== COMPLETE URL ENDPOINTS ====================

    /**
     * Complete URL for authentication test endpoint
     */
    public static final String COMPLETE_AUTH_TEST_URL = BASE_URL + AUTH_TEST_ENDPOINT;

    /**
     * Complete URL for authentication login endpoint
     */
    public static final String COMPLETE_AUTH_LOGIN_URL = BASE_URL + AUTH_LOGIN_ENDPOINT;

    /**
     * Complete URL for authentication signup endpoint
     */
    public static final String COMPLETE_AUTH_SIGNUP_URL = BASE_URL + AUTH_SIGNUP_ENDPOINT;

    /**
     * Complete URL for authentication refresh endpoint
     */
    public static final String COMPLETE_AUTH_REFRESH_URL = BASE_URL + AUTH_REFRESH_ENDPOINT;

    /**
     * Complete URL for health check endpoint
     */
    public static final String COMPLETE_HEALTH_CHECK_URL = BASE_URL + "/actuator/health";
}
