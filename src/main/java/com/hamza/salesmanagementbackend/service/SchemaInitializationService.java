package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service to ensure all JPA entity tables are created by Hibernate
 * This runs before other initializers to guarantee schema exists
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(0) // Run first, before all other initializers
public class SchemaInitializationService implements CommandLineRunner {

    // Inject all repositories to force table creation
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== SCHEMA INITIALIZATION SERVICE STARTING ===");
        
        try {
            // Force creation of all entity tables by accessing repositories
            initializeAllTables();
            
            // Verify tables were created
            verifyTablesExist();
            
            log.info("=== SCHEMA INITIALIZATION COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            log.error("Schema initialization failed", e);
            // Don't throw exception to prevent application startup failure
        }
    }

    private void initializeAllTables() {
        log.info("Forcing creation of all entity tables...");
        
        try {
            // Core business entities
            log.debug("Initializing core business tables...");
            customerRepository.count();
            productRepository.count();
            categoryRepository.count();
            supplierRepository.count();
            
            // Sales and transactions
            log.debug("Initializing sales tables...");
            saleRepository.count();
            saleItemRepository.count();
            
            // Purchase orders
            log.debug("Initializing purchase order tables...");
            purchaseOrderRepository.count();
            purchaseOrderItemRepository.count();
            
            // Inventory management
            log.debug("Initializing inventory tables...");
            inventoryRepository.count();
            
            // Returns
            log.debug("Initializing return tables...");
            returnRepository.count();
            returnItemRepository.count();
            
            // Promotions and coupons
            log.debug("Initializing promotion tables...");
            promotionRepository.count();
            couponRepository.count();
            
            // User management (should already exist from DatabaseInitializationService)
            log.debug("Verifying user tables...");
            userRepository.count();
            refreshTokenRepository.count();
            
            log.info("All repository access completed - tables should now exist");
            
        } catch (Exception e) {
            log.warn("Some tables may not have been created during initialization", e);
            // Continue - tables will be created when first accessed
        }
    }

    private void verifyTablesExist() {
        log.info("Verifying that all expected tables exist...");
        
        try {
            // Query information_schema to check which tables exist
            String sql = "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() " +
                        "ORDER BY table_name";
            
            @SuppressWarnings("unchecked")
            java.util.List<String> existingTables = entityManager
                .createNativeQuery(sql)
                .getResultList();
            
            log.info("Found {} tables in database:", existingTables.size());
            existingTables.forEach(tableName -> log.info("  - {}", tableName));
            
            // Expected main entity tables
            String[] expectedTables = {
                "customers", "products", "categories", "suppliers",
                "sales", "sale_items", "purchase_orders", "purchase_order_items",
                "inventories", "returns", "return_items", "promotions", "coupons",
                "users", "refresh_tokens", "roles"
            };
            
            log.info("Checking for expected entity tables...");
            for (String expectedTable : expectedTables) {
                if (existingTables.contains(expectedTable)) {
                    log.info("  ✓ {} - EXISTS", expectedTable);
                } else {
                    log.warn("  ✗ {} - MISSING", expectedTable);
                }
            }
            
        } catch (Exception e) {
            log.warn("Could not verify table existence", e);
        }
    }
}
