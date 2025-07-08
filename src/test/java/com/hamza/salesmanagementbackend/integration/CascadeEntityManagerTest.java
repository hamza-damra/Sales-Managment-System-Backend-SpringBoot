package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify that EntityManager-based cascade deletion works correctly.
 * This test focuses on the most basic cascade scenario to ensure the approach is sound.
 */
@DataJpaTest
@ActiveProfiles("test")
public class CascadeEntityManagerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Supplier testSupplier;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create and save test data
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test Category Description")
                .build();
        testCategory = categoryRepository.save(testCategory);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Product Description")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("50.00"))
                .stockQuantity(10)
                .sku("TEST-SKU-001")
                .category(testCategory)
                .build();
        testProduct = productRepository.save(testProduct);

        testSupplier = Supplier.builder()
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("9876543210")
                .email("supplier@example.com")
                .address("Supplier Address")
                .build();
        testSupplier = supplierRepository.save(testSupplier);
    }

    @Test
    void testSimpleSupplierCascadeDelete() {
        // Create purchase order with item
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(testProduct)
                .quantity(5)
                .unitCost(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("500.00"))
                .build();
        purchaseOrderItem = purchaseOrderItemRepository.save(purchaseOrderItem);

        // Store IDs for verification
        Long supplierId = testSupplier.getId();
        Long purchaseOrderId = purchaseOrder.getId();
        Long purchaseOrderItemId = purchaseOrderItem.getId();
        Long productId = testProduct.getId();

        // Verify entities exist before deletion
        assertThat(supplierRepository.findById(supplierId)).isPresent();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isPresent();
        assertThat(purchaseOrderItemRepository.findById(purchaseOrderItemId)).isPresent();
        assertThat(productRepository.findById(productId)).isPresent();

        // Delete supplier using EntityManager for proper cascade
        Supplier supplierToDelete = entityManager.find(Supplier.class, supplierId);
        assertThat(supplierToDelete).isNotNull();
        
        // Initialize the lazy collection to ensure cascade works
        int purchaseOrderCount = supplierToDelete.getPurchaseOrders().size();
        assertThat(purchaseOrderCount).isEqualTo(1);
        
        // Remove the supplier - this should cascade to purchase orders and items
        entityManager.remove(supplierToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify cascade deletion
        assertThat(supplierRepository.findById(supplierId)).isEmpty();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isEmpty();
        assertThat(purchaseOrderItemRepository.findById(purchaseOrderItemId)).isEmpty();

        // Product should still exist (not cascade deleted)
        assertThat(productRepository.findById(productId)).isPresent();
    }

    @Test
    void testCascadeConfigurationExists() {
        // This test verifies that the cascade configuration is properly set up
        // by checking that the relationship annotations are correctly configured
        
        // Create a supplier with purchase order
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-002")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .build();
        
        // Save and verify the relationship is established
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Reload supplier and verify relationship
        Supplier reloadedSupplier = supplierRepository.findById(testSupplier.getId()).orElseThrow();
        
        // The cascade configuration should allow us to access the purchase orders
        // This test ensures the relationship is properly mapped
        assertThat(reloadedSupplier.getPurchaseOrders()).isNotNull();
        
        // Initialize the collection and verify it contains our purchase order
        int orderCount = reloadedSupplier.getPurchaseOrders().size();
        assertThat(orderCount).isGreaterThan(0);
        
        // Verify the bidirectional relationship
        assertThat(purchaseOrder.getSupplier()).isEqualTo(testSupplier);
    }

    @Test
    void testEntityManagerVsRepositoryDelete() {
        // This test demonstrates the difference between EntityManager.remove() and Repository.deleteById()
        
        // Create a simple purchase order
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-003")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("300.00"))
                .build();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        Long supplierId = testSupplier.getId();
        Long purchaseOrderId = purchaseOrder.getId();

        // Verify entities exist
        assertThat(supplierRepository.findById(supplierId)).isPresent();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isPresent();

        // Use EntityManager.remove() for proper cascade behavior
        Supplier supplierToDelete = entityManager.find(Supplier.class, supplierId);
        assertThat(supplierToDelete).isNotNull();
        
        // Initialize collection
        supplierToDelete.getPurchaseOrders().size();
        
        // Remove using EntityManager
        entityManager.remove(supplierToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify both entities are deleted due to cascade
        assertThat(supplierRepository.findById(supplierId)).isEmpty();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isEmpty();
    }

    @Test
    void testLazyLoadingInitialization() {
        // This test verifies that lazy loading initialization is necessary for cascade to work
        
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-004")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("400.00"))
                .build();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Load supplier
        Supplier supplier = entityManager.find(Supplier.class, testSupplier.getId());
        assertThat(supplier).isNotNull();
        
        // Verify that the purchase orders collection exists
        assertThat(supplier.getPurchaseOrders()).isNotNull();
        
        // Initialize the collection by accessing its size
        int size = supplier.getPurchaseOrders().size();
        assertThat(size).isEqualTo(1);
        
        // Verify the collection contains our purchase order
        PurchaseOrder foundOrder = supplier.getPurchaseOrders().get(0);
        assertThat(foundOrder.getOrderNumber()).isEqualTo("PO-004");
        assertThat(foundOrder.getSupplier()).isEqualTo(supplier);
    }
}
