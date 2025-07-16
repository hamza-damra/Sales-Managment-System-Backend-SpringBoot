package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import com.hamza.salesmanagementbackend.service.CategoryMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Order(2) // Run after UserDataInitializer
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMigrationService categoryMigrationService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Force Hibernate to create all entity tables by accessing repositories
            log.info("Initializing database schema by accessing all repositories...");
            initializeSchema();

            // Check if data initialization is needed
            if (customerRepository.count() == 0) {
                log.info("No existing data found, initializing sample data...");
                initializeData();
            } else {
                log.info("Existing data found, skipping data initialization");
            }

            // Run category migration and setup
            categoryMigrationService.createDefaultCategories();
            categoryMigrationService.migrateStringCategoriesToEntities();
            categoryMigrationService.assignUncategorizedProducts();
            categoryMigrationService.validateCategoryMigration();

        } catch (Exception e) {
            log.error("Error during data initialization", e);
            // Don't throw exception to prevent application startup failure
        }
    }

    /**
     * Force Hibernate to create all entity tables by accessing repositories
     * This ensures all tables exist before we try to query them
     */
    private void initializeSchema() {
        try {
            log.info("Accessing all repositories to trigger table creation...");

            // Access each repository to force table creation
            customerRepository.count();
            productRepository.count();
            saleRepository.count();
            saleItemRepository.count();
            categoryRepository.count();

            log.info("All main entity tables should now be created");

        } catch (Exception e) {
            log.warn("Some tables may not have been created during schema initialization", e);
            // Continue anyway - tables will be created when first accessed
        }
    }

    private void initializeData() {
        // Create sample customers
        Customer customer1 = new Customer("John Doe", "john.doe@email.com", "+1234567890", "123 Main St, City, State");
        Customer customer2 = new Customer("Jane Smith", "jane.smith@email.com", "+1234567891", "456 Oak Ave, City, State");
        Customer customer3 = new Customer("Bob Johnson", "bob.johnson@email.com", "+1234567892", "789 Pine Rd, City, State");
        Customer customer4 = new Customer("Alice Brown", "alice.brown@email.com", "+1234567893", "321 Elm St, City, State");
        Customer customer5 = new Customer("Charlie Wilson", "charlie.wilson@email.com", "+1234567894", "654 Maple Dr, City, State");

        customerRepository.saveAll(Arrays.asList(customer1, customer2, customer3, customer4, customer5));

        // Create sample products (categories will be assigned during migration)
        Product product1 = Product.builder()
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .sku("LAP001")
                .build();

        Product product2 = Product.builder()
                .name("Mouse")
                .description("Wireless optical mouse")
                .price(new BigDecimal("29.99"))
                .stockQuantity(100)
                .sku("MOU001")
                .build();

        Product product3 = Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("79.99"))
                .stockQuantity(75)
                .sku("KEY001")
                .build();

        Product product4 = Product.builder()
                .name("Monitor")
                .description("24-inch LED monitor")
                .price(new BigDecimal("199.99"))
                .stockQuantity(30)
                .sku("MON001")
                .build();

        Product product5 = Product.builder()
                .name("Headphones")
                .description("Noise-cancelling headphones")
                .price(new BigDecimal("149.99"))
                .stockQuantity(40)
                .sku("HEA001")
                .build();

        Product product6 = Product.builder()
                .name("Smartphone")
                .description("Latest smartphone")
                .price(new BigDecimal("699.99"))
                .stockQuantity(25)
                .sku("PHO001")
                .build();

        Product product7 = Product.builder()
                .name("Tablet")
                .description("10-inch tablet")
                .price(new BigDecimal("399.99"))
                .stockQuantity(35)
                .sku("TAB001")
                .build();

        Product product8 = Product.builder()
                .name("Speaker")
                .description("Bluetooth speaker")
                .price(new BigDecimal("89.99"))
                .stockQuantity(60)
                .sku("SPE001")
                .build();

        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4, product5, product6, product7, product8));

        // Assign Electronics category to all products after creation
        Category electronicsCategory = categoryRepository.findByNameIgnoreCase("Electronics")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name("Electronics")
                            .description("Electronic devices and accessories")
                            .status(Category.CategoryStatus.ACTIVE)
                            .displayOrder(1)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        // Update all products to use the Electronics category
        for (Product product : Arrays.asList(product1, product2, product3, product4, product5, product6, product7, product8)) {
            product.setCategory(electronicsCategory);
            productRepository.save(product);
        }

        // Create sample sales
        createSampleSales();
    }

    private void createSampleSales() {
        // Get all customers and products from the database
        Customer[] customers = customerRepository.findAll().toArray(new Customer[0]);
        Product[] products = productRepository.findAll().toArray(new Product[0]);

        // Sale 1 - Completed sale with enhanced features
        Sale sale1 = new Sale(customers[0]);
        sale1.setSaleDate(LocalDateTime.now().minusDays(5));
        sale1.setStatus(SaleStatus.COMPLETED);
        sale1.setPaymentMethod(Sale.PaymentMethod.CREDIT_CARD);
        sale1.setPaymentStatus(Sale.PaymentStatus.PAID);
        sale1.setSaleType(Sale.SaleType.RETAIL);
        sale1.setSalesChannel("Online");
        sale1.setBillingAddress(customers[0].getAddress());
        sale1.setShippingAddress(customers[0].getAddress());
        sale1.setDiscountPercentage(BigDecimal.valueOf(5.0)); // 5% discount
        sale1.setTaxPercentage(BigDecimal.valueOf(8.5)); // 8.5% tax
        sale1 = saleRepository.save(sale1);

        // Create sale items with enhanced features
        SaleItem item1_1 = new SaleItem(sale1, products[0], 1, products[0].getPrice());
        item1_1.setDiscountPercentage(BigDecimal.valueOf(5.0));
        item1_1.setTaxPercentage(BigDecimal.valueOf(8.5));
        item1_1.calculateTotals();

        SaleItem item1_2 = new SaleItem(sale1, products[1], 2, products[1].getPrice());
        item1_2.setDiscountPercentage(BigDecimal.valueOf(5.0));
        item1_2.setTaxPercentage(BigDecimal.valueOf(8.5));
        item1_2.calculateTotals();

        saleItemRepository.saveAll(Arrays.asList(item1_1, item1_2));

        // Update sale totals
        sale1.setItems(Arrays.asList(item1_1, item1_2));
        sale1.calculateTotals();
        sale1.processLoyaltyPoints();
        saleRepository.save(sale1);

        // Sale 2 - Pending payment
        Sale sale2 = new Sale(customers[1]);
        sale2.setSaleDate(LocalDateTime.now().minusDays(3));
        sale2.setStatus(SaleStatus.PENDING);
        sale2.setPaymentMethod(Sale.PaymentMethod.BANK_TRANSFER);
        sale2.setPaymentStatus(Sale.PaymentStatus.PENDING);
        sale2.setSaleType(Sale.SaleType.B2B);
        sale2.setSalesChannel("In-store");
        sale2.setBillingAddress(customers[1].getAddress());
        sale2.setTaxPercentage(BigDecimal.valueOf(8.5));
        sale2 = saleRepository.save(sale2);

        SaleItem item2_1 = new SaleItem(sale2, products[2], 1, products[2].getPrice());
        item2_1.setTaxPercentage(BigDecimal.valueOf(8.5));
        item2_1.calculateTotals();

        SaleItem item2_2 = new SaleItem(sale2, products[3], 1, products[3].getPrice());
        item2_2.setTaxPercentage(BigDecimal.valueOf(8.5));
        item2_2.calculateTotals();

        saleItemRepository.saveAll(Arrays.asList(item2_1, item2_2));

        sale2.setItems(Arrays.asList(item2_1, item2_2));
        sale2.calculateTotals();
        saleRepository.save(sale2);

        // Sale 3 - Gift sale with shipping
        Sale sale3 = new Sale(customers[2]);
        sale3.setSaleDate(LocalDateTime.now().minusDays(1));
        sale3.setStatus(SaleStatus.COMPLETED);
        sale3.setPaymentMethod(Sale.PaymentMethod.PAYPAL);
        sale3.setPaymentStatus(Sale.PaymentStatus.PAID);
        sale3.setSaleType(Sale.SaleType.ONLINE);
        sale3.setSalesChannel("Website");
        sale3.setBillingAddress(customers[2].getAddress());
        sale3.setShippingAddress("456 Gift Address, Gift City, State");
        sale3.setIsGift(true);
        sale3.setGiftMessage("Happy Birthday!");
        sale3.setShippingCost(BigDecimal.valueOf(15.99));
        sale3.setTaxPercentage(BigDecimal.valueOf(8.5));
        sale3.setDeliveryStatus(Sale.DeliveryStatus.SHIPPED);
        sale3.setTrackingNumber("TRK123456789");
        sale3 = saleRepository.save(sale3);

        SaleItem item3_1 = new SaleItem(sale3, products[4], 1, products[4].getPrice());
        item3_1.setTaxPercentage(BigDecimal.valueOf(8.5));
        item3_1.setWarrantyInfo("1 year manufacturer warranty");
        item3_1.calculateTotals();

        saleItemRepository.save(item3_1);

        sale3.setItems(Arrays.asList(item3_1));
        sale3.calculateTotals();
        sale3.processLoyaltyPoints();
        saleRepository.save(sale3);

        // Sale 4 - Wholesale sale with discount
        Sale sale4 = new Sale(customers[3]);
        sale4.setSaleDate(LocalDateTime.now().minusHours(6));
        sale4.setStatus(SaleStatus.COMPLETED);
        sale4.setPaymentMethod(Sale.PaymentMethod.CHECK);
        sale4.setPaymentStatus(Sale.PaymentStatus.PAID);
        sale4.setSaleType(Sale.SaleType.WHOLESALE);
        sale4.setSalesChannel("Sales Rep");
        sale4.setBillingAddress(customers[3].getAddress());
        sale4.setDiscountPercentage(BigDecimal.valueOf(15.0)); // 15% wholesale discount
        sale4.setTaxPercentage(BigDecimal.valueOf(8.5));
        sale4.setSalesPerson("John Sales Rep");
        sale4 = saleRepository.save(sale4);

        SaleItem item4_1 = new SaleItem(sale4, products[5], 2, products[5].getPrice());
        item4_1.setDiscountPercentage(BigDecimal.valueOf(15.0));
        item4_1.setTaxPercentage(BigDecimal.valueOf(8.5));
        item4_1.calculateTotals();

        SaleItem item4_2 = new SaleItem(sale4, products[6], 1, products[6].getPrice());
        item4_2.setDiscountPercentage(BigDecimal.valueOf(15.0));
        item4_2.setTaxPercentage(BigDecimal.valueOf(8.5));
        item4_2.calculateTotals();

        saleItemRepository.saveAll(Arrays.asList(item4_1, item4_2));

        sale4.setItems(Arrays.asList(item4_1, item4_2));
        sale4.calculateTotals();
        sale4.processLoyaltyPoints();
        saleRepository.save(sale4);

        // Sale 5 - Recent cash sale
        Sale sale5 = new Sale(customers[4]);
        sale5.setSaleDate(LocalDateTime.now().minusMinutes(30));
        sale5.setStatus(SaleStatus.COMPLETED);
        sale5.setPaymentMethod(Sale.PaymentMethod.CASH);
        sale5.setPaymentStatus(Sale.PaymentStatus.PAID);
        sale5.setSaleType(Sale.SaleType.RETAIL);
        sale5.setSalesChannel("In-store");
        sale5.setBillingAddress(customers[4].getAddress());
        sale5.setTaxPercentage(BigDecimal.valueOf(8.5));
        sale5 = saleRepository.save(sale5);

        SaleItem item5_1 = new SaleItem(sale5, products[7], 1, products[7].getPrice());
        item5_1.setTaxPercentage(BigDecimal.valueOf(8.5));
        item5_1.calculateTotals();

        saleItemRepository.save(item5_1);

        sale5.setItems(Arrays.asList(item5_1));
        sale5.calculateTotals();
        sale5.processLoyaltyPoints();
        saleRepository.save(sale5);

        System.out.println("Sample data initialized with enhanced features:");
        System.out.println("- " + customerRepository.count() + " customers");
        System.out.println("- " + productRepository.count() + " products");
        System.out.println("- " + saleRepository.count() + " sales");
        System.out.println("- " + saleItemRepository.count() + " sale items");

        // Print actual IDs for testing purposes
        System.out.println("\n=== TESTING REFERENCE DATA ===");
        System.out.println("Customer IDs: " + customerRepository.findAll().stream()
                .map(c -> c.getId() + " (" + c.getName() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("Product IDs: " + productRepository.findAll().stream()
                .map(p -> p.getId() + " (" + p.getName() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("Sale IDs: " + saleRepository.findAll().stream()
                .map(s -> s.getId() + " (Customer: " + s.getCustomer().getId() + ", Status: " + s.getStatus() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("Sale Item IDs: " + saleItemRepository.findAll().stream()
                .map(si -> si.getId() + " (Sale: " + si.getSale().getId() + ", Product: " + si.getProduct().getId() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("===============================\n");
    }
}
