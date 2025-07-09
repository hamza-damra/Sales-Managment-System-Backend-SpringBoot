import java.math.BigDecimal;

public class BigDecimalTestFix {
    public static void main(String[] args) {
        System.out.println("=== BigDecimal Scale Issue Demonstration ===");
        System.out.println();
        
        // Demonstrate the issue
        BigDecimal zero1 = BigDecimal.ZERO;  // scale = 0
        BigDecimal zero2 = BigDecimal.valueOf(0.00);  // scale = 2
        
        System.out.println("BigDecimal.ZERO: " + zero1 + " (scale: " + zero1.scale() + ")");
        System.out.println("BigDecimal.valueOf(0.00): " + zero2 + " (scale: " + zero2.scale() + ")");
        System.out.println("Are they equal with equals()? " + zero1.equals(zero2));
        System.out.println("Are they equal with compareTo()? " + (zero1.compareTo(zero2) == 0));
        System.out.println();
        
        // Demonstrate the subtraction that causes the issue
        BigDecimal originalAmount = BigDecimal.ZERO;  // scale = 0
        BigDecimal discountAmount = BigDecimal.valueOf(0.00);  // scale = 2
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);
        
        System.out.println("Original Amount: " + originalAmount + " (scale: " + originalAmount.scale() + ")");
        System.out.println("Discount Amount: " + discountAmount + " (scale: " + discountAmount.scale() + ")");
        System.out.println("Final Amount (original - discount): " + finalAmount + " (scale: " + finalAmount.scale() + ")");
        System.out.println();
        
        // Show the fix
        System.out.println("=== Fix Approaches ===");
        System.out.println("1. Use same scale values:");
        BigDecimal originalFixed = BigDecimal.valueOf(0.00);  // scale = 2
        BigDecimal discountFixed = BigDecimal.valueOf(0.00);  // scale = 2
        BigDecimal finalFixed = originalFixed.subtract(discountFixed);
        System.out.println("   Final Amount: " + finalFixed + " (scale: " + finalFixed.scale() + ")");
        System.out.println("   Equals BigDecimal.valueOf(0.00)? " + finalFixed.equals(BigDecimal.valueOf(0.00)));
        System.out.println();
        
        System.out.println("2. Use compareTo() for comparison:");
        System.out.println("   finalAmount.compareTo(BigDecimal.ZERO) == 0? " + (finalAmount.compareTo(BigDecimal.ZERO) == 0));
        System.out.println();
        
        System.out.println("Fix applied: Use BigDecimal.valueOf(0.00) for both values in test");
    }
}
