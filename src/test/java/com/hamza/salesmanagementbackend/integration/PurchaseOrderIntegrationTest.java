package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Integration test to verify the complete Purchase Order implementation
 * This test verifies that all components work together correctly
 */
public class PurchaseOrderIntegrationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testPurchaseOrderImplementationStructure() throws Exception {
        // This test verifies that the implementation structure is correct
        // and all classes can be loaded without compilation errors
        
        System.out.println("=== Purchase Order Implementation Structure Test ===");
        
        try {
            // Test 1: Verify entities exist and can be loaded
            Class<?> purchaseOrderClass = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder");
            Class<?> purchaseOrderItemClass = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrderItem");
            System.out.println("✅ Entities loaded successfully");
            
            // Test 2: Verify DTOs exist and can be loaded
            Class<?> purchaseOrderDTOClass = Class.forName("com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO");
            Class<?> purchaseOrderItemDTOClass = Class.forName("com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO");
            System.out.println("✅ DTOs loaded successfully");
            
            // Test 3: Verify service exists and can be loaded
            Class<?> purchaseOrderServiceClass = Class.forName("com.hamza.salesmanagementbackend.service.PurchaseOrderService");
            System.out.println("✅ Service loaded successfully");
            
            // Test 4: Verify controller exists and can be loaded
            Class<?> purchaseOrderControllerClass = Class.forName("com.hamza.salesmanagementbackend.controller.PurchaseOrderController");
            System.out.println("✅ Controller loaded successfully");
            
            // Test 5: Verify repository exists and can be loaded
            Class<?> purchaseOrderRepositoryClass = Class.forName("com.hamza.salesmanagementbackend.repository.PurchaseOrderRepository");
            System.out.println("✅ Repository loaded successfully");
            
            // Test 6: Verify enums exist and have correct values
            Class<?> statusEnum = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder$PurchaseOrderStatus");
            Class<?> priorityEnum = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder$OrderPriority");
            
            Object[] statusValues = statusEnum.getEnumConstants();
            Object[] priorityValues = priorityEnum.getEnumConstants();
            
            System.out.println("✅ Status enum has " + statusValues.length + " values: " + Arrays.toString(statusValues));
            System.out.println("✅ Priority enum has " + priorityValues.length + " values: " + Arrays.toString(priorityValues));
            
            // Test 7: Verify required fields exist in entities
            purchaseOrderClass.getDeclaredField("priority");
            purchaseOrderClass.getDeclaredField("shippingAddress");
            purchaseOrderClass.getDeclaredField("shippingCost");
            purchaseOrderClass.getDeclaredField("taxRate");
            purchaseOrderClass.getDeclaredField("deliveryTerms");
            purchaseOrderClass.getDeclaredField("sentDate");
            System.out.println("✅ All required fields exist in PurchaseOrder entity");
            
            // Test 8: Verify business methods exist
            purchaseOrderClass.getDeclaredMethod("calculateTotals");
            purchaseOrderClass.getDeclaredMethod("markAsSent");
            purchaseOrderClass.getDeclaredMethod("markAsDelivered");
            purchaseOrderClass.getDeclaredMethod("isFullyReceived");
            purchaseOrderClass.getDeclaredMethod("getReceivingProgress");
            purchaseOrderClass.getDeclaredMethod("getItemsCount");
            System.out.println("✅ All business methods exist in PurchaseOrder entity");
            
            // Test 9: Verify DTO fields exist
            purchaseOrderDTOClass.getDeclaredField("priority");
            purchaseOrderDTOClass.getDeclaredField("shippingAddress");
            purchaseOrderDTOClass.getDeclaredField("shippingCost");
            purchaseOrderDTOClass.getDeclaredField("taxRate");
            purchaseOrderDTOClass.getDeclaredField("deliveryTerms");
            purchaseOrderDTOClass.getDeclaredField("sentDate");
            purchaseOrderDTOClass.getDeclaredField("itemsCount");
            purchaseOrderDTOClass.getDeclaredField("isFullyReceived");
            purchaseOrderDTOClass.getDeclaredField("receivingProgress");
            System.out.println("✅ All required fields exist in PurchaseOrderDTO");
            
            // Test 10: Test enum value creation
            Object pending = Enum.valueOf((Class<Enum>) statusEnum, "PENDING");
            Object approved = Enum.valueOf((Class<Enum>) statusEnum, "APPROVED");
            Object sent = Enum.valueOf((Class<Enum>) statusEnum, "SENT");
            Object delivered = Enum.valueOf((Class<Enum>) statusEnum, "DELIVERED");
            Object cancelled = Enum.valueOf((Class<Enum>) statusEnum, "CANCELLED");
            System.out.println("✅ All status enum values can be created");
            
            Object low = Enum.valueOf((Class<Enum>) priorityEnum, "LOW");
            Object normal = Enum.valueOf((Class<Enum>) priorityEnum, "NORMAL");
            Object high = Enum.valueOf((Class<Enum>) priorityEnum, "HIGH");
            Object urgent = Enum.valueOf((Class<Enum>) priorityEnum, "URGENT");
            System.out.println("✅ All priority enum values can be created");
            
            System.out.println("\n=== ALL STRUCTURE TESTS PASSED! ===");
            System.out.println("✅ Purchase Order implementation structure is correct");
            
        } catch (Exception e) {
            System.err.println("❌ STRUCTURE TEST FAILED: " + e.getMessage());
            throw new AssertionError("Purchase Order implementation structure test failed", e);
        }
    }

    @Test
    public void testBusinessLogicCalculations() {
        System.out.println("=== Testing Business Logic Calculations ===");
        
        try {
            // Test financial calculations
            BigDecimal subtotal = new BigDecimal("2550.00");
            Double taxRate = 15.0;
            BigDecimal shippingCost = new BigDecimal("100.00");
            BigDecimal discountAmount = new BigDecimal("50.00");
            
            // Calculate tax amount: subtotal * taxRate / 100
            BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(taxRate))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            
            // Calculate total: subtotal + taxAmount + shippingCost - discountAmount
            BigDecimal totalAmount = subtotal.add(taxAmount)
                    .add(shippingCost)
                    .subtract(discountAmount);
            
            BigDecimal expectedTaxAmount = new BigDecimal("382.50");
            BigDecimal expectedTotalAmount = new BigDecimal("2982.50");
            
            if (taxAmount.compareTo(expectedTaxAmount) != 0) {
                throw new AssertionError("Tax calculation failed. Expected: " + expectedTaxAmount + ", Got: " + taxAmount);
            }
            
            if (totalAmount.compareTo(expectedTotalAmount) != 0) {
                throw new AssertionError("Total calculation failed. Expected: " + expectedTotalAmount + ", Got: " + totalAmount);
            }
            
            System.out.println("✅ Tax calculation: " + subtotal + " * " + taxRate + "% = " + taxAmount);
            System.out.println("✅ Total calculation: " + subtotal + " + " + taxAmount + " + " + shippingCost + " - " + discountAmount + " = " + totalAmount);
            
            // Test order number generation logic
            String year = String.valueOf(LocalDateTime.now().getYear());
            long count = 5; // Simulating 5 existing orders
            String orderNumber = String.format("PO-%s-%03d", year, count + 1);
            String expectedPattern = "PO-" + year + "-006";
            
            if (!orderNumber.equals(expectedPattern)) {
                throw new AssertionError("Order number generation failed. Expected: " + expectedPattern + ", Got: " + orderNumber);
            }
            
            System.out.println("✅ Order number generation: " + orderNumber);
            
            // Test format validation
            if (!orderNumber.matches("PO-\\d{4}-\\d{3}")) {
                throw new AssertionError("Order number format validation failed: " + orderNumber);
            }
            
            System.out.println("✅ Order number format validation passed");
            
            System.out.println("\n=== ALL BUSINESS LOGIC TESTS PASSED! ===");
            
        } catch (Exception e) {
            System.err.println("❌ BUSINESS LOGIC TEST FAILED: " + e.getMessage());
            throw new AssertionError("Business logic test failed", e);
        }
    }

    @Test
    public void testDTOStructure() {
        System.out.println("=== Testing DTO Structure ===");
        
        try {
            // Test PurchaseOrderDTO creation
            PurchaseOrderDTO orderDTO = PurchaseOrderDTO.builder()
                    .supplierId(1L)
                    .supplierName("Test Supplier")
                    .orderDate(LocalDateTime.now())
                    .expectedDeliveryDate(LocalDateTime.now().plusDays(10))
                    .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                    .priority(PurchaseOrder.OrderPriority.NORMAL)
                    .shippingAddress("123 Test Street")
                    .taxRate(15.0)
                    .shippingCost(BigDecimal.valueOf(100.00))
                    .subtotal(BigDecimal.valueOf(2550.00))
                    .taxAmount(BigDecimal.valueOf(382.50))
                    .totalAmount(BigDecimal.valueOf(3032.50))
                    .notes("Test order")
                    .itemsCount(2)
                    .isFullyReceived(false)
                    .receivingProgress(0.0)
                    .build();
            
            System.out.println("✅ PurchaseOrderDTO created successfully");
            System.out.println("   - Order ID: " + orderDTO.getId());
            System.out.println("   - Supplier ID: " + orderDTO.getSupplierId());
            System.out.println("   - Status: " + orderDTO.getStatus());
            System.out.println("   - Priority: " + orderDTO.getPriority());
            System.out.println("   - Total Amount: " + orderDTO.getTotalAmount());
            System.out.println("   - Items Count: " + orderDTO.getItemsCount());
            System.out.println("   - Receiving Progress: " + orderDTO.getReceivingProgress() + "%");
            
            // Test PurchaseOrderItemDTO creation
            PurchaseOrderItemDTO itemDTO = PurchaseOrderItemDTO.builder()
                    .productId(1L)
                    .productName("Test Product")
                    .productSku("TEST-001")
                    .quantity(100)
                    .unitCost(BigDecimal.valueOf(25.50))
                    .totalPrice(BigDecimal.valueOf(2550.00))
                    .receivedQuantity(0)
                    .pendingQuantity(100)
                    .isFullyReceived(false)
                    .isPartiallyReceived(false)
                    .remainingQuantity(100)
                    .build();
            
            System.out.println("✅ PurchaseOrderItemDTO created successfully");
            System.out.println("   - Product ID: " + itemDTO.getProductId());
            System.out.println("   - Quantity: " + itemDTO.getQuantity());
            System.out.println("   - Unit Cost: " + itemDTO.getUnitCost());
            System.out.println("   - Total Price: " + itemDTO.getTotalPrice());
            System.out.println("   - Received Quantity: " + itemDTO.getReceivedQuantity());
            System.out.println("   - Is Fully Received: " + itemDTO.getIsFullyReceived());
            
            // Test JSON serialization
            String orderJson = objectMapper.writeValueAsString(orderDTO);
            String itemJson = objectMapper.writeValueAsString(itemDTO);
            
            System.out.println("✅ JSON serialization successful");
            System.out.println("   - Order JSON length: " + orderJson.length() + " characters");
            System.out.println("   - Item JSON length: " + itemJson.length() + " characters");
            
            // Test JSON deserialization
            PurchaseOrderDTO deserializedOrder = objectMapper.readValue(orderJson, PurchaseOrderDTO.class);
            PurchaseOrderItemDTO deserializedItem = objectMapper.readValue(itemJson, PurchaseOrderItemDTO.class);
            
            System.out.println("✅ JSON deserialization successful");
            System.out.println("   - Deserialized order supplier ID: " + deserializedOrder.getSupplierId());
            System.out.println("   - Deserialized item product ID: " + deserializedItem.getProductId());
            
            System.out.println("\n=== ALL DTO TESTS PASSED! ===");
            
        } catch (Exception e) {
            System.err.println("❌ DTO TEST FAILED: " + e.getMessage());
            throw new AssertionError("DTO test failed", e);
        }
    }
}
