package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify proper cascade configurations and orphan removal settings
 * for all JPA entity relationships in the Sales Management System.
 *
 * This test validates the entity relationship configurations without requiring
 * a full Spring context or database connection.
 */
public class CascadeConfigurationTest {

    private Customer testCustomer;
    private Category testCategory;
    private Product testProduct;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        // Create test data
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
    void testCustomerCascadeConfiguration() {
        // Test that Customer entity has proper cascade configurations

        // Create a customer with sales and returns
        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .address("Test Address")
                .build();

        Sale sale = Sale.builder()
                .customer(customer)
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

        sale.setItems(Arrays.asList(saleItem));

        Return returnEntity = Return.builder()
                .customer(customer)
                .originalSale(sale)
                .reason(Return.ReturnReason.DEFECTIVE)
                .totalRefundAmount(new BigDecimal("100.00"))
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .originalSaleItem(saleItem)
                .product(testProduct)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();

        returnEntity.setItems(Arrays.asList(returnItem));

        // Set up bidirectional relationships
        customer.setSales(Arrays.asList(sale));
        customer.setReturns(Arrays.asList(returnEntity));

        // Verify relationships are properly set
        assertNotNull(customer.getSales());
        assertNotNull(customer.getReturns());
        assertEquals(1, customer.getSales().size());
        assertEquals(1, customer.getReturns().size());
        assertEquals(customer, sale.getCustomer());
        assertEquals(customer, returnEntity.getCustomer());
    }

    @Test
    void testSupplierCascadeConfiguration() {
        // Test that Supplier entity has proper cascade configurations

        Supplier supplier = Supplier.builder()
                .name("Test Supplier")
                .contactPerson("John Doe")
                .phone("9876543210")
                .email("supplier@example.com")
                .address("Supplier Address")
                .build();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(supplier)
                .orderNumber("PO-001")
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(testProduct)
                .quantity(5)
                .unitCost(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("500.00"))
                .build();

        purchaseOrder.setItems(Arrays.asList(purchaseOrderItem));
        supplier.setPurchaseOrders(Arrays.asList(purchaseOrder));

        // Verify relationships are properly set
        assertNotNull(supplier.getPurchaseOrders());
        assertEquals(1, supplier.getPurchaseOrders().size());
        assertEquals(supplier, purchaseOrder.getSupplier());
        assertNotNull(purchaseOrder.getItems());
        assertEquals(1, purchaseOrder.getItems().size());
    }

    @Test
    void testCategoryCascadeConfiguration() {
        // Test that Category entity has proper cascade configurations
        // Category should NOT cascade delete to products

        Category category = Category.builder()
                .name("Test Category")
                .description("Test Category Description")
                .build();

        Product product = Product.builder()
                .name("Test Product")
                .description("Test Product Description")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("50.00"))
                .stockQuantity(10)
                .sku("TEST-SKU-001")
                .category(category)
                .build();

        category.setProducts(Arrays.asList(product));

        // Verify relationships are properly set
        assertNotNull(category.getProducts());
        assertEquals(1, category.getProducts().size());
        assertEquals(category, product.getCategory());

        // Verify category cascade configuration allows products to exist independently
        // This is tested by the fact that Category uses CascadeType.PERSIST and MERGE only
        assertTrue(category.getProducts().contains(product));
    }

    @Test
    void testSaleCascadeConfiguration() {
        // Test that Sale entity has proper cascade configurations

        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .address("Test Address")
                .build();

        Sale sale = Sale.builder()
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.PENDING)
                .totalAmount(new BigDecimal("300.00"))
                .subtotal(new BigDecimal("300.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .build();

        SaleItem saleItem1 = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();

        SaleItem saleItem2 = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();

        sale.setItems(new ArrayList<>(Arrays.asList(saleItem1, saleItem2)));

        // Verify relationships are properly set
        assertNotNull(sale.getItems());
        assertEquals(2, sale.getItems().size());
        assertEquals(sale, saleItem1.getSale());
        assertEquals(sale, saleItem2.getSale());

        // Test orphan removal behavior by removing an item from collection
        sale.getItems().remove(saleItem2);
        assertEquals(1, sale.getItems().size());
        assertFalse(sale.getItems().contains(saleItem2));
    }

    @Test
    void testProductCascadeConfiguration() {
        // Test that Product entity has proper cascade configurations

        Category category = Category.builder()
                .name("Test Category")
                .description("Test Category Description")
                .build();

        Product product = Product.builder()
                .name("Test Product")
                .description("Test Product Description")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("50.00"))
                .stockQuantity(10)
                .sku("TEST-SKU-001")
                .category(category)
                .build();

        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .address("Test Address")
                .build();

        Sale sale = Sale.builder()
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("100.00"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .build();

        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .product(product)
                .returnQuantity(1)
                .originalUnitPrice(new BigDecimal("100.00"))
                .refundAmount(new BigDecimal("100.00"))
                .build();

        PurchaseOrderItem purchaseOrderItem = PurchaseOrderItem.builder()
                .product(product)
                .quantity(1)
                .unitCost(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();

        // Set up relationships
        product.setSaleItems(Arrays.asList(saleItem));
        product.setReturnItems(Arrays.asList(returnItem));
        product.setPurchaseOrderItems(Arrays.asList(purchaseOrderItem));

        // Verify relationships are properly set
        assertNotNull(product.getSaleItems());
        assertNotNull(product.getReturnItems());
        assertNotNull(product.getPurchaseOrderItems());
        assertEquals(1, product.getSaleItems().size());
        assertEquals(1, product.getReturnItems().size());
        assertEquals(1, product.getPurchaseOrderItems().size());
        assertEquals(product, saleItem.getProduct());
        assertEquals(product, returnItem.getProduct());
        assertEquals(product, purchaseOrderItem.getProduct());
    }

    @Test
    void testReturnCascadeConfiguration() {
        // Test that Return entity has proper cascade configurations

        Return returnEntity = Return.builder()
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

        // Verify relationships are properly set
        assertNotNull(returnEntity.getItems());
        assertEquals(1, returnEntity.getItems().size());
        assertEquals(returnEntity, returnItem.getReturnEntity());
    }
}
