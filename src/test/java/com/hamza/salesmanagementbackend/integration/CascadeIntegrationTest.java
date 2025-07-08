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
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify cascade configurations work correctly with the database.
 */
@DataJpaTest
@ActiveProfiles("test")
public class CascadeIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private ReturnItemRepository returnItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Customer testCustomer;
    private Category testCategory;
    private Product testProduct;
    private Supplier testSupplier;

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

        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .address("Test Address")
                .build();
        testCustomer = customerRepository.save(testCustomer);

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
    void testCustomerCascadeDelete() {
        // Create sale with items
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();
        sale = saleRepository.save(sale);

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();
        saleItem = saleItemRepository.save(saleItem);

        // Create return
        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();
        returnEntity = returnRepository.save(returnEntity);

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .originalSaleItem(saleItem)
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();
        returnItem = returnItemRepository.save(returnItem);

        // Store IDs for verification
        Long customerId = testCustomer.getId();
        Long saleId = sale.getId();
        Long saleItemId = saleItem.getId();
        Long returnId = returnEntity.getId();
        Long returnItemId = returnItem.getId();
        Long productId = testProduct.getId();

        // Delete customer - should cascade delete sales, sale items, returns, and return items
        // Use EntityManager to properly handle cascade deletion
        Customer customerToDelete = entityManager.find(Customer.class, customerId);
        assertThat(customerToDelete).isNotNull();

        // Initialize the lazy collections to ensure cascade works
        customerToDelete.getSales().size(); // This initializes the collection
        customerToDelete.getReturns().size(); // This initializes the collection

        entityManager.remove(customerToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify cascade deletion
        assertThat(customerRepository.findById(customerId)).isEmpty();
        assertThat(saleRepository.findById(saleId)).isEmpty();
        assertThat(saleItemRepository.findById(saleItemId)).isEmpty();
        assertThat(returnRepository.findById(returnId)).isEmpty();
        assertThat(returnItemRepository.findById(returnItemId)).isEmpty();

        // Product should still exist (not cascade deleted)
        assertThat(productRepository.findById(productId)).isPresent();
    }

    @Test
    void testSupplierCascadeDelete() {
        // Create purchase order with items
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

        // Delete supplier - should cascade delete purchase orders and items
        // Use EntityManager to properly handle cascade deletion
        Supplier supplierToDelete = entityManager.find(Supplier.class, supplierId);
        assertThat(supplierToDelete).isNotNull();

        // Initialize the lazy collection to ensure cascade works
        supplierToDelete.getPurchaseOrders().size(); // This initializes the collection

        entityManager.remove(supplierToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify cascade deletion
        assertThat(supplierRepository.findById(supplierId)).isEmpty();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isEmpty();
        assertThat(purchaseOrderItemRepository.findById(purchaseOrderItemId)).isEmpty();

        // Product should still exist
        assertThat(productRepository.findById(productId)).isPresent();
    }

    @Test
    void testCategoryDeletionDoesNotCascadeToProducts() {
        Long categoryId = testCategory.getId();
        Long productId = testProduct.getId();

        // Delete category - should NOT cascade delete products
        // Use EntityManager to properly handle limited cascade behavior
        Category categoryToDelete = entityManager.find(Category.class, categoryId);
        assertThat(categoryToDelete).isNotNull();

        // Initialize the lazy collection (but this should NOT cascade delete products)
        categoryToDelete.getProducts().size(); // This initializes the collection

        entityManager.remove(categoryToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify category is deleted but product still exists
        assertThat(categoryRepository.findById(categoryId)).isEmpty();

        // Product should still exist but with null category
        Optional<Product> productOpt = productRepository.findById(productId);
        assertThat(productOpt).isPresent();

        // Refresh the product to see the updated state
        Product refreshedProduct = productRepository.findById(productId).get();
        assertThat(refreshedProduct.getCategory()).isNull();
    }

    @Test
    void testProductCascadeDelete() {
        // Create related entities
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("100.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();
        sale = saleRepository.save(sale);

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();
        saleItem = saleItemRepository.save(saleItem);

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-002")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(testProduct)
                .quantity(1)
                .unitCost(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();
        purchaseOrderItem = purchaseOrderItemRepository.save(purchaseOrderItem);

        // Store IDs for verification
        Long productId = testProduct.getId();
        Long saleId = sale.getId();
        Long saleItemId = saleItem.getId();
        Long purchaseOrderId = purchaseOrder.getId();
        Long purchaseOrderItemId = purchaseOrderItem.getId();

        // Delete product - should cascade delete related sale items and purchase order items
        // Use EntityManager to properly handle cascade deletion
        Product productToDelete = entityManager.find(Product.class, productId);
        assertThat(productToDelete).isNotNull();

        // Initialize the lazy collections to ensure cascade works
        productToDelete.getSaleItems().size(); // This initializes the collection
        productToDelete.getPurchaseOrderItems().size(); // This initializes the collection

        entityManager.remove(productToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify cascade deletion
        assertThat(productRepository.findById(productId)).isEmpty();
        assertThat(saleItemRepository.findById(saleItemId)).isEmpty();
        assertThat(purchaseOrderItemRepository.findById(purchaseOrderItemId)).isEmpty();

        // Parent entities should still exist
        assertThat(saleRepository.findById(saleId)).isPresent();
        assertThat(purchaseOrderRepository.findById(purchaseOrderId)).isPresent();
    }

    @Test
    void testReturnCascadeDelete() {
        // Create sale and return
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("100.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();
        sale = saleRepository.save(sale);

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();
        saleItem = saleItemRepository.save(saleItem);

        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();
        returnEntity = returnRepository.save(returnEntity);

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .originalSaleItem(saleItem)
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();
        returnItem = returnItemRepository.save(returnItem);

        // Store IDs for verification
        Long returnId = returnEntity.getId();
        Long returnItemId = returnItem.getId();
        Long saleId = sale.getId();
        Long saleItemId = saleItem.getId();

        // Delete return - should cascade delete return items
        // Use EntityManager to properly handle cascade deletion
        Return returnToDelete = entityManager.find(Return.class, returnId);
        assertThat(returnToDelete).isNotNull();

        // Initialize the lazy collection to ensure cascade works
        returnToDelete.getItems().size(); // This initializes the collection

        entityManager.remove(returnToDelete);
        entityManager.flush();
        entityManager.clear();

        // Verify cascade deletion
        assertThat(returnRepository.findById(returnId)).isEmpty();
        assertThat(returnItemRepository.findById(returnItemId)).isEmpty();

        // Sale and sale items should still exist
        assertThat(saleRepository.findById(saleId)).isPresent();
        assertThat(saleItemRepository.findById(saleItemId)).isPresent();
    }
}
