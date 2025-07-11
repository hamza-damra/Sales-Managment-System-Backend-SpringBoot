package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for PurchaseOrder entity to verify the implementation
 */
public class PurchaseOrderEntityTest {

    private PurchaseOrder purchaseOrder;
    private Supplier supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup test supplier
        supplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .email("supplier@test.com")
                .status(Supplier.SupplierStatus.ACTIVE)
                .paymentTerms("NET_30")
                .deliveryTerms("FOB_DESTINATION")
                .build();

        // Setup test product
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(25.50))
                .build();

        // Setup test purchase order
        purchaseOrder = PurchaseOrder.builder()
                .orderNumber("PO-2024-001")
                .supplier(supplier)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(10))
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .priority(PurchaseOrder.OrderPriority.NORMAL)
                .shippingAddress("123 Test Street")
                .taxRate(15.0)
                .shippingCost(BigDecimal.valueOf(100.00))
                .discountAmount(BigDecimal.valueOf(50.00))
                .build();
    }

    @Test
    void testPurchaseOrderCreation() {
        System.out.println("🧪 Testing PurchaseOrder entity creation...");
        
        assertThat(purchaseOrder).isNotNull();
        assertThat(purchaseOrder.getOrderNumber()).isEqualTo("PO-2024-001");
        assertThat(purchaseOrder.getSupplier()).isEqualTo(supplier);
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrder.PurchaseOrderStatus.PENDING);
        assertThat(purchaseOrder.getPriority()).isEqualTo(PurchaseOrder.OrderPriority.NORMAL);
        assertThat(purchaseOrder.getTaxRate()).isEqualTo(15.0);
        assertThat(purchaseOrder.getShippingCost()).isEqualTo(BigDecimal.valueOf(100.00));
        
        System.out.println("✅ PurchaseOrder entity creation test passed");
    }

    @Test
    void testStatusEnumValues() {
        System.out.println("🧪 Testing PurchaseOrder status enum values...");
        
        // Test all status values
        assertThat(PurchaseOrder.PurchaseOrderStatus.PENDING).isNotNull();
        assertThat(PurchaseOrder.PurchaseOrderStatus.APPROVED).isNotNull();
        assertThat(PurchaseOrder.PurchaseOrderStatus.SENT).isNotNull();
        assertThat(PurchaseOrder.PurchaseOrderStatus.DELIVERED).isNotNull();
        assertThat(PurchaseOrder.PurchaseOrderStatus.CANCELLED).isNotNull();
        
        // Test enum count
        PurchaseOrder.PurchaseOrderStatus[] statuses = PurchaseOrder.PurchaseOrderStatus.values();
        assertThat(statuses).hasSize(5);
        
        System.out.println("   ✓ Status enum values: " + java.util.Arrays.toString(statuses));
        System.out.println("✅ Status enum test passed");
    }

    @Test
    void testPriorityEnumValues() {
        System.out.println("🧪 Testing PurchaseOrder priority enum values...");
        
        // Test all priority values
        assertThat(PurchaseOrder.OrderPriority.LOW).isNotNull();
        assertThat(PurchaseOrder.OrderPriority.NORMAL).isNotNull();
        assertThat(PurchaseOrder.OrderPriority.HIGH).isNotNull();
        assertThat(PurchaseOrder.OrderPriority.URGENT).isNotNull();
        
        // Test enum count
        PurchaseOrder.OrderPriority[] priorities = PurchaseOrder.OrderPriority.values();
        assertThat(priorities).hasSize(4);
        
        System.out.println("   ✓ Priority enum values: " + java.util.Arrays.toString(priorities));
        System.out.println("✅ Priority enum test passed");
    }

    @Test
    void testCalculateTotals() {
        System.out.println("🧪 Testing calculateTotals method...");
        
        // Create test items
        List<PurchaseOrderItem> items = new ArrayList<>();
        
        PurchaseOrderItem item1 = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(product)
                .quantity(100)
                .unitCost(BigDecimal.valueOf(25.50))
                .build();
        item1.calculateTotals();
        
        PurchaseOrderItem item2 = PurchaseOrderItem.builder()
                .purchaseOrder(purchaseOrder)
                .product(product)
                .quantity(50)
                .unitCost(BigDecimal.valueOf(45.00))
                .build();
        item2.calculateTotals();
        
        items.add(item1);
        items.add(item2);
        purchaseOrder.setItems(items);
        
        // Calculate totals
        purchaseOrder.calculateTotals();
        
        // Verify calculations
        BigDecimal expectedSubtotal = BigDecimal.valueOf(4800.00); // (100 * 25.50) + (50 * 45.00)
        BigDecimal expectedTaxAmount = BigDecimal.valueOf(720.00); // 4800 * 15%
        BigDecimal expectedTotal = BigDecimal.valueOf(5570.00); // 4800 + 720 + 100 - 50
        
        assertThat(purchaseOrder.getSubtotal()).isEqualByComparingTo(expectedSubtotal);
        assertThat(purchaseOrder.getTaxAmount()).isEqualByComparingTo(expectedTaxAmount);
        assertThat(purchaseOrder.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        
        System.out.println("   ✓ Subtotal: " + purchaseOrder.getSubtotal());
        System.out.println("   ✓ Tax amount: " + purchaseOrder.getTaxAmount());
        System.out.println("   ✓ Total amount: " + purchaseOrder.getTotalAmount());
        System.out.println("✅ Calculate totals test passed");
    }

    @Test
    void testStatusTransitions() {
        System.out.println("🧪 Testing status transition methods...");
        
        // Test approve
        purchaseOrder.approve("manager@test.com");
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrder.PurchaseOrderStatus.APPROVED);
        assertThat(purchaseOrder.getApprovedBy()).isEqualTo("manager@test.com");
        assertThat(purchaseOrder.getApprovedDate()).isNotNull();
        
        System.out.println("   ✓ Approve transition: PENDING → APPROVED");
        
        // Test mark as sent
        purchaseOrder.markAsSent();
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrder.PurchaseOrderStatus.SENT);
        assertThat(purchaseOrder.getSentDate()).isNotNull();
        
        System.out.println("   ✓ Sent transition: APPROVED → SENT");
        
        // Test mark as delivered
        purchaseOrder.markAsDelivered();
        assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseOrder.PurchaseOrderStatus.DELIVERED);
        assertThat(purchaseOrder.getActualDeliveryDate()).isNotNull();
        
        System.out.println("   ✓ Delivered transition: SENT → DELIVERED");
        System.out.println("✅ Status transition test passed");
    }

    @Test
    void testBusinessLogicMethods() {
        System.out.println("🧪 Testing business logic methods...");
        
        // Test canBeModified
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.PENDING);
        assertThat(purchaseOrder.canBeModified()).isTrue();
        
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.DELIVERED);
        assertThat(purchaseOrder.canBeModified()).isFalse();
        
        System.out.println("   ✓ canBeModified logic works correctly");
        
        // Test canBeCancelled
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.PENDING);
        assertThat(purchaseOrder.canBeCancelled()).isTrue();
        
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.CANCELLED);
        assertThat(purchaseOrder.canBeCancelled()).isFalse();
        
        System.out.println("   ✓ canBeCancelled logic works correctly");
        
        // Test getItemsCount
        List<PurchaseOrderItem> items = new ArrayList<>();
        items.add(PurchaseOrderItem.builder().build());
        items.add(PurchaseOrderItem.builder().build());
        purchaseOrder.setItems(items);
        
        assertThat(purchaseOrder.getItemsCount()).isEqualTo(2);
        
        System.out.println("   ✓ getItemsCount returns correct count");
        
        // Test isOverdue
        purchaseOrder.setExpectedDeliveryDate(LocalDateTime.now().minusDays(1));
        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.SENT);
        assertThat(purchaseOrder.isOverdue()).isTrue();
        
        purchaseOrder.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1));
        assertThat(purchaseOrder.isOverdue()).isFalse();
        
        System.out.println("   ✓ isOverdue logic works correctly");
        System.out.println("✅ Business logic methods test passed");
    }

    @Test
    void testReceivingProgress() {
        System.out.println("🧪 Testing receiving progress calculation...");
        
        // Create test items with different received quantities
        List<PurchaseOrderItem> items = new ArrayList<>();
        
        PurchaseOrderItem item1 = PurchaseOrderItem.builder()
                .quantity(100)
                .receivedQuantity(50) // 50% received
                .build();
        
        PurchaseOrderItem item2 = PurchaseOrderItem.builder()
                .quantity(200)
                .receivedQuantity(100) // 50% received
                .build();
        
        items.add(item1);
        items.add(item2);
        purchaseOrder.setItems(items);
        
        // Total quantity: 300, Total received: 150, Progress: 50%
        double progress = purchaseOrder.getReceivingProgress();
        assertThat(progress).isEqualTo(50.0);
        
        System.out.println("   ✓ Receiving progress: " + progress + "%");
        
        // Test fully received
        item1.setReceivedQuantity(100);
        item2.setReceivedQuantity(200);
        
        assertThat(purchaseOrder.isFullyReceived()).isTrue();
        assertThat(purchaseOrder.getReceivingProgress()).isEqualTo(100.0);
        
        System.out.println("   ✓ Fully received status works correctly");
        System.out.println("✅ Receiving progress test passed");
    }
}
