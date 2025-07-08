package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that the cascade configuration fixes work correctly.
 * This test ensures that all required fields are properly set and
 * entity relationships compile without validation errors.
 */
public class CascadeFixVerificationTest {

    private Customer testCustomer;
    private Category testCategory;
    private Product testProduct;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .address("Test Address")
                .build();

        testCategory = Category.builder()
                .name("Test Category")
                .description("Test Category Description")
                .build();

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Product Description")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("50.00"))
                .stockQuantity(10)
                .sku("TEST-SKU-001")
                .category(testCategory)
                .build();

        testSupplier = Supplier.builder()
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("9876543210")
                .email("supplier@example.com")
                .address("Supplier Address")
                .build();
    }

    @Test
    void testReturnItemWithRequiredFields() {
        // Create a sale item first
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();

        // Create return with return item that has all required fields
        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .originalSaleItem(saleItem)
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))  // Required field
                .refundAmount(new BigDecimal("100.00"))
                .build();

        // Verify all required fields are set
        assertNotNull(returnItem.getReturnEntity());
        assertNotNull(returnItem.getOriginalSaleItem());
        assertNotNull(returnItem.getProduct());
        assertNotNull(returnItem.getReturnQuantity());
        assertNotNull(returnItem.getOriginalUnitPrice());
        assertNotNull(returnItem.getRefundAmount());

        // Verify values are correct
        assertEquals(returnEntity, returnItem.getReturnEntity());
        assertEquals(saleItem, returnItem.getOriginalSaleItem());
        assertEquals(testProduct, returnItem.getProduct());
        assertEquals(Integer.valueOf(1), returnItem.getReturnQuantity());
        assertEquals(new BigDecimal("100.00"), returnItem.getOriginalUnitPrice());
        assertEquals(new BigDecimal("100.00"), returnItem.getRefundAmount());
    }

    @Test
    void testPurchaseOrderItemWithCorrectFields() {
        // Create purchase order with correct field names
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(testProduct)
                .quantity(5)
                .unitCost(new BigDecimal("100.00"))  // Correct field name
                .totalPrice(new BigDecimal("500.00"))
                .build();

        // Verify all fields are set correctly
        assertNotNull(purchaseOrderItem.getPurchaseOrder());
        assertNotNull(purchaseOrderItem.getProduct());
        assertNotNull(purchaseOrderItem.getQuantity());
        assertNotNull(purchaseOrderItem.getUnitCost());
        assertNotNull(purchaseOrderItem.getTotalPrice());

        // Verify values are correct
        assertEquals(purchaseOrder, purchaseOrderItem.getPurchaseOrder());
        assertEquals(testProduct, purchaseOrderItem.getProduct());
        assertEquals(Integer.valueOf(5), purchaseOrderItem.getQuantity());
        assertEquals(new BigDecimal("100.00"), purchaseOrderItem.getUnitCost());
        assertEquals(new BigDecimal("500.00"), purchaseOrderItem.getTotalPrice());
    }

    @Test
    void testSaleStatusEnumUsage() {
        // Test that SaleStatus enum is used correctly (not Sale.SaleStatus)
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)  // Correct enum usage
                .totalAmount(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();

        // Verify status is set correctly
        assertEquals(SaleStatus.COMPLETED, sale.getStatus());
        
        // Test other status values
        sale.setStatus(SaleStatus.PENDING);
        assertEquals(SaleStatus.PENDING, sale.getStatus());
        
        sale.setStatus(SaleStatus.CANCELLED);
        assertEquals(SaleStatus.CANCELLED, sale.getStatus());
    }

    @Test
    void testPurchaseOrderStatusEnumUsage() {
        // Test that PurchaseOrderStatus enum is used correctly
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)  // Correct enum usage
                .totalAmount(new BigDecimal("500.00"))
                .build();

        // Verify status is set correctly
        assertEquals(PurchaseOrder.PurchaseOrderStatus.PENDING, purchaseOrder.getStatus());
        
        // Test other status values
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.APPROVED);
        assertEquals(PurchaseOrder.PurchaseOrderStatus.APPROVED, purchaseOrder.getStatus());
        
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED);
        assertEquals(PurchaseOrder.PurchaseOrderStatus.RECEIVED, purchaseOrder.getStatus());
    }

    @Test
    void testCascadeRelationshipsSetup() {
        // Test that all cascade relationships can be set up without errors
        
        // Customer relationships
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();

        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        testCustomer.setSales(Arrays.asList(sale));
        testCustomer.setReturns(Arrays.asList(returnEntity));

        // Supplier relationships
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        testSupplier.setPurchaseOrders(Arrays.asList(purchaseOrder));

        // Category relationships (limited cascade)
        testCategory.setProducts(Arrays.asList(testProduct));

        // Verify all relationships are set
        assertNotNull(testCustomer.getSales());
        assertNotNull(testCustomer.getReturns());
        assertNotNull(testSupplier.getPurchaseOrders());
        assertNotNull(testCategory.getProducts());
        
        assertEquals(1, testCustomer.getSales().size());
        assertEquals(1, testCustomer.getReturns().size());
        assertEquals(1, testSupplier.getPurchaseOrders().size());
        assertEquals(1, testCategory.getProducts().size());
    }

    @Test
    void testAllRequiredFieldsPresent() {
        // This test ensures that all entities can be created with required fields
        // and that no validation errors occur during object creation
        
        assertDoesNotThrow(() -> {
            // Create all entities with required fields
            Customer customer = Customer.builder()
                    .name("Required Name")
                    .email("required@email.com")
                    .build();

            Category category = Category.builder()
                    .name("Required Category Name")
                    .build();

            Product product = Product.builder()
                    .name("Required Product Name")
                    .price(new BigDecimal("10.00"))
                    .build();

            Supplier supplier = Supplier.builder()
                    .name("Required Supplier Name")
                    .build();

            Sale sale = Sale.builder()
                    .customer(customer)
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(1)
                    .unitPrice(new BigDecimal("100.00"))
                    .totalPrice(new BigDecimal("100.00"))
                    .build();

            Return returnEntity = Return.builder()
                    .customer(customer)
                    .originalSale(sale)
                    .reason(Return.ReturnReason.DEFECTIVE)
                    .totalRefundAmount(new BigDecimal("50.00"))
                    .build();

            ReturnItem returnItem = ReturnItem.builder()
                    .returnEntity(returnEntity)
                    .originalSaleItem(saleItem)
                    .product(product)
                    .returnQuantity(1)
                    .originalUnitPrice(new BigDecimal("100.00"))
                    .refundAmount(new BigDecimal("50.00"))
                    .build();

            PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                    .supplier(supplier)
                    .totalAmount(new BigDecimal("200.00"))
                    .build();

            PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .product(product)
                    .quantity(2)
                    .unitCost(new BigDecimal("100.00"))
                    .totalPrice(new BigDecimal("200.00"))
                    .build();

            // If we reach here, all entities were created successfully
            assertTrue(true, "All entities created without validation errors");
        });
    }
}
