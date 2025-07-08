package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple compilation test to verify that all cascade configurations compile correctly
 * and entity relationships are properly defined.
 */
public class CascadeCompilationTest {

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
    void testCustomerRelationshipsCompile() {
        // Test Customer -> Sales relationship
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("200.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();

        // Test Customer -> Returns relationship
        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        // Set up bidirectional relationships
        testCustomer.setSales(Arrays.asList(sale));
        testCustomer.setReturns(Arrays.asList(returnEntity));

        // Verify relationships compile and work
        assertNotNull(testCustomer.getSales());
        assertNotNull(testCustomer.getReturns());
        assertEquals(1, testCustomer.getSales().size());
        assertEquals(1, testCustomer.getReturns().size());
    }

    @Test
    void testProductRelationshipsCompile() {
        // Test Product -> SaleItems relationship
        SaleItem saleItem = SaleItem.builder()
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();

        // Test Product -> ReturnItems relationship
        ReturnItem returnItem = ReturnItem.builder()
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();

        // Test Product -> PurchaseOrderItems relationship
        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .product(testProduct)
                .quantity(5)
                .unitCost(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("250.00"))
                .build();

        // Set up relationships
        testProduct.setSaleItems(Arrays.asList(saleItem));
        testProduct.setReturnItems(Arrays.asList(returnItem));
        testProduct.setPurchaseOrderItems(Arrays.asList(purchaseOrderItem));

        // Verify relationships compile and work
        assertNotNull(testProduct.getSaleItems());
        assertNotNull(testProduct.getReturnItems());
        assertNotNull(testProduct.getPurchaseOrderItems());
        assertEquals(1, testProduct.getSaleItems().size());
        assertEquals(1, testProduct.getReturnItems().size());
        assertEquals(1, testProduct.getPurchaseOrderItems().size());
    }

    @Test
    void testSupplierRelationshipsCompile() {
        // Test Supplier -> PurchaseOrders relationship
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        testSupplier.setPurchaseOrders(Arrays.asList(purchaseOrder));

        // Verify relationships compile and work
        assertNotNull(testSupplier.getPurchaseOrders());
        assertEquals(1, testSupplier.getPurchaseOrders().size());
        assertEquals(testSupplier, purchaseOrder.getSupplier());
    }

    @Test
    void testCategoryRelationshipsCompile() {
        // Test Category -> Products relationship (limited cascade)
        testCategory.setProducts(Arrays.asList(testProduct));

        // Verify relationships compile and work
        assertNotNull(testCategory.getProducts());
        assertEquals(1, testCategory.getProducts().size());
        assertEquals(testCategory, testProduct.getCategory());
    }

    @Test
    void testSaleRelationshipsCompile() {
        // Test Sale -> SaleItems relationship
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.PENDING)
                .totalAmount(new BigDecimal("300.00"))
                .subtotal(new BigDecimal("300.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .build();

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("300.00"))
                .build();

        // Test Sale -> Returns relationship
        Return returnEntity = Return.builder()
                .originalSale(sale)
                .customer(testCustomer)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        sale.setItems(Arrays.asList(saleItem));
        sale.setReturns(Arrays.asList(returnEntity));

        // Verify relationships compile and work
        assertNotNull(sale.getItems());
        assertNotNull(sale.getReturns());
        assertEquals(1, sale.getItems().size());
        assertEquals(1, sale.getReturns().size());
    }

    @Test
    void testReturnRelationshipsCompile() {
        // Test Return -> ReturnItems relationship
        Return returnEntity = Return.builder()
                .customer(testCustomer)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();

        returnEntity.setItems(Arrays.asList(returnItem));

        // Verify relationships compile and work
        assertNotNull(returnEntity.getItems());
        assertEquals(1, returnEntity.getItems().size());
        assertEquals(returnEntity, returnItem.getReturnEntity());
    }

    @Test
    void testPurchaseOrderRelationshipsCompile() {
        // Test PurchaseOrder -> PurchaseOrderItems relationship
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(testSupplier)
                .orderNumber("PO-002")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("250.00"))
                .build();

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(testProduct)
                .quantity(5)
                .unitCost(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("250.00"))
                .build();

        purchaseOrder.setItems(Arrays.asList(purchaseOrderItem));

        // Verify relationships compile and work
        assertNotNull(purchaseOrder.getItems());
        assertEquals(1, purchaseOrder.getItems().size());
        assertEquals(purchaseOrder, purchaseOrderItem.getPurchaseOrder());
    }
}
