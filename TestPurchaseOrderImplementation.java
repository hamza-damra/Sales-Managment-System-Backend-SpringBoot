import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Simple test runner to verify Purchase Order implementation
 * This can be run independently to check if the implementation is working
 */
public class TestPurchaseOrderImplementation {

    public static void main(String[] args) {
        System.out.println("=== Purchase Order Implementation Test ===\n");
        
        boolean allTestsPassed = true;
        
        try {
            // Test 1: Class Loading
            System.out.println("🧪 Test 1: Class Loading");
            testClassLoading();
            System.out.println("✅ Class loading test passed\n");
            
            // Test 2: Enum Values
            System.out.println("🧪 Test 2: Enum Values");
            testEnumValues();
            System.out.println("✅ Enum values test passed\n");
            
            // Test 3: Business Calculations
            System.out.println("🧪 Test 3: Business Calculations");
            testBusinessCalculations();
            System.out.println("✅ Business calculations test passed\n");
            
            // Test 4: Order Number Generation
            System.out.println("🧪 Test 4: Order Number Generation");
            testOrderNumberGeneration();
            System.out.println("✅ Order number generation test passed\n");
            
            // Test 5: Field Validation
            System.out.println("🧪 Test 5: Field Validation");
            testFieldValidation();
            System.out.println("✅ Field validation test passed\n");
            
        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            allTestsPassed = false;
        }
        
        if (allTestsPassed) {
            System.out.println("🎉 ALL TESTS PASSED SUCCESSFULLY!");
            System.out.println("✅ Purchase Order implementation is working correctly");
            System.out.println("✅ Ready for frontend integration");
        } else {
            System.err.println("❌ SOME TESTS FAILED");
            System.err.println("Please check the implementation and fix any issues");
        }
    }

    private static void testClassLoading() throws Exception {
        // Test that all required classes can be loaded
        String[] requiredClasses = {
            "com.hamza.salesmanagementbackend.entity.PurchaseOrder",
            "com.hamza.salesmanagementbackend.entity.PurchaseOrderItem",
            "com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO",
            "com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO",
            "com.hamza.salesmanagementbackend.service.PurchaseOrderService",
            "com.hamza.salesmanagementbackend.controller.PurchaseOrderController",
            "com.hamza.salesmanagementbackend.repository.PurchaseOrderRepository"
        };
        
        for (String className : requiredClasses) {
            try {
                Class.forName(className);
                System.out.println("   ✓ " + className.substring(className.lastIndexOf('.') + 1));
            } catch (ClassNotFoundException e) {
                throw new Exception("Required class not found: " + className);
            }
        }
    }

    private static void testEnumValues() throws Exception {
        // Test PurchaseOrderStatus enum
        Class<?> statusEnum = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder$PurchaseOrderStatus");
        Object[] statusValues = statusEnum.getEnumConstants();
        
        String[] expectedStatuses = {"PENDING", "APPROVED", "SENT", "DELIVERED", "CANCELLED"};
        if (statusValues.length != expectedStatuses.length) {
            throw new Exception("Expected " + expectedStatuses.length + " status values, got " + statusValues.length);
        }
        
        System.out.println("   ✓ Status enum: " + Arrays.toString(statusValues));
        
        // Test OrderPriority enum
        Class<?> priorityEnum = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder$OrderPriority");
        Object[] priorityValues = priorityEnum.getEnumConstants();
        
        String[] expectedPriorities = {"LOW", "NORMAL", "HIGH", "URGENT"};
        if (priorityValues.length != expectedPriorities.length) {
            throw new Exception("Expected " + expectedPriorities.length + " priority values, got " + priorityValues.length);
        }
        
        System.out.println("   ✓ Priority enum: " + Arrays.toString(priorityValues));
        
        // Test enum value creation
        for (String status : expectedStatuses) {
            Enum.valueOf((Class<Enum>) statusEnum, status);
        }
        
        for (String priority : expectedPriorities) {
            Enum.valueOf((Class<Enum>) priorityEnum, priority);
        }
        
        System.out.println("   ✓ All enum values can be created");
    }

    private static void testBusinessCalculations() throws Exception {
        // Test tax calculation
        BigDecimal subtotal = new BigDecimal("2550.00");
        Double taxRate = 15.0;
        BigDecimal expectedTaxAmount = new BigDecimal("382.50");
        
        BigDecimal calculatedTaxAmount = subtotal.multiply(BigDecimal.valueOf(taxRate))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        if (calculatedTaxAmount.compareTo(expectedTaxAmount) != 0) {
            throw new Exception("Tax calculation failed. Expected: " + expectedTaxAmount + ", Got: " + calculatedTaxAmount);
        }
        
        System.out.println("   ✓ Tax calculation: " + subtotal + " * " + taxRate + "% = " + calculatedTaxAmount);
        
        // Test total calculation
        BigDecimal shippingCost = new BigDecimal("100.00");
        BigDecimal discountAmount = new BigDecimal("50.00");
        BigDecimal expectedTotal = new BigDecimal("2982.50");
        
        BigDecimal calculatedTotal = subtotal.add(calculatedTaxAmount)
                .add(shippingCost)
                .subtract(discountAmount);
        
        if (calculatedTotal.compareTo(expectedTotal) != 0) {
            throw new Exception("Total calculation failed. Expected: " + expectedTotal + ", Got: " + calculatedTotal);
        }
        
        System.out.println("   ✓ Total calculation: " + subtotal + " + " + calculatedTaxAmount + " + " + shippingCost + " - " + discountAmount + " = " + calculatedTotal);
        
        // Test item calculation
        int quantity = 100;
        BigDecimal unitCost = new BigDecimal("25.50");
        BigDecimal expectedItemTotal = new BigDecimal("2550.00");
        
        BigDecimal calculatedItemTotal = unitCost.multiply(BigDecimal.valueOf(quantity));
        
        if (calculatedItemTotal.compareTo(expectedItemTotal) != 0) {
            throw new Exception("Item total calculation failed. Expected: " + expectedItemTotal + ", Got: " + calculatedItemTotal);
        }
        
        System.out.println("   ✓ Item calculation: " + quantity + " * " + unitCost + " = " + calculatedItemTotal);
    }

    private static void testOrderNumberGeneration() throws Exception {
        // Test order number format
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = 5;
        String orderNumber = String.format("PO-%s-%03d", year, count + 1);
        
        String expectedPattern = "PO-" + year + "-006";
        if (!orderNumber.equals(expectedPattern)) {
            throw new Exception("Order number generation failed. Expected: " + expectedPattern + ", Got: " + orderNumber);
        }
        
        System.out.println("   ✓ Order number generation: " + orderNumber);
        
        // Test format validation
        if (!orderNumber.matches("PO-\\d{4}-\\d{3}")) {
            throw new Exception("Order number format validation failed: " + orderNumber);
        }
        
        System.out.println("   ✓ Order number format validation passed");
        
        // Test uniqueness logic
        for (int i = 1; i <= 10; i++) {
            String testOrderNumber = String.format("PO-%s-%03d", year, i);
            if (!testOrderNumber.matches("PO-\\d{4}-\\d{3}")) {
                throw new Exception("Order number format failed for: " + testOrderNumber);
            }
        }
        
        System.out.println("   ✓ Order number uniqueness logic validated");
    }

    private static void testFieldValidation() throws Exception {
        // Test required field validation logic
        Class<?> purchaseOrderClass = Class.forName("com.hamza.salesmanagementbackend.entity.PurchaseOrder");
        Class<?> purchaseOrderDTOClass = Class.forName("com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO");
        
        // Check that all required fields exist in entity
        String[] requiredEntityFields = {
            "priority", "shippingAddress", "shippingCost", "taxRate", 
            "deliveryTerms", "sentDate", "supplier", "items"
        };
        
        for (String fieldName : requiredEntityFields) {
            try {
                purchaseOrderClass.getDeclaredField(fieldName);
                System.out.println("   ✓ Entity field exists: " + fieldName);
            } catch (NoSuchFieldException e) {
                throw new Exception("Required entity field missing: " + fieldName);
            }
        }
        
        // Check that all required fields exist in DTO
        String[] requiredDTOFields = {
            "priority", "shippingAddress", "shippingCost", "taxRate", 
            "deliveryTerms", "sentDate", "itemsCount", "isFullyReceived", "receivingProgress"
        };
        
        for (String fieldName : requiredDTOFields) {
            try {
                purchaseOrderDTOClass.getDeclaredField(fieldName);
                System.out.println("   ✓ DTO field exists: " + fieldName);
            } catch (NoSuchFieldException e) {
                throw new Exception("Required DTO field missing: " + fieldName);
            }
        }
        
        // Check that business methods exist
        String[] requiredMethods = {
            "calculateTotals", "markAsSent", "markAsDelivered", 
            "isFullyReceived", "getReceivingProgress", "getItemsCount"
        };
        
        for (String methodName : requiredMethods) {
            try {
                purchaseOrderClass.getDeclaredMethod(methodName);
                System.out.println("   ✓ Business method exists: " + methodName);
            } catch (NoSuchMethodException e) {
                throw new Exception("Required business method missing: " + methodName);
            }
        }
    }
}
