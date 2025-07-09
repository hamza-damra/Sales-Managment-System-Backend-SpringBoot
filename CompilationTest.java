import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple test to verify that the Map.of() compilation issues are fixed
 */
public class CompilationTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Map.of() compilation fixes...");
        
        // Test 1: Mixed types that would cause compilation error with Map.of()
        testMixedTypesWithHashMap();
        
        // Test 2: Same types that work fine with Map.of()
        testSameTypesWithMapOf();
        
        System.out.println("All compilation tests passed!");
    }
    
    private static void testMixedTypesWithHashMap() {
        System.out.println("\nTest 1: Mixed types using HashMap (fixed approach)");
        
        // This is the fixed approach - using HashMap for mixed types
        Map<String, Object> mixedMap = new HashMap<>();
        mixedMap.put("productName", "Test Product");           // String
        mixedMap.put("quantitySold", 42);                      // Integer
        mixedMap.put("revenue", BigDecimal.valueOf(1000.50));  // BigDecimal
        
        System.out.println("  Mixed types map created successfully: " + mixedMap);
    }
    
    private static void testSameTypesWithMapOf() {
        System.out.println("\nTest 2: Same types using Map.of() (works fine)");
        
        // This works fine with Map.of() because all values are Strings
        Map<String, String> stringMap = Map.of(
                "name", "Test Product",
                "category", "Electronics",
                "status", "Active"
        );
        
        System.out.println("  String map created successfully: " + stringMap);
    }
    
    private static void demonstrateOriginalProblem() {
        System.out.println("\nDemonstrating the original problem (commented out to avoid compilation error):");
        System.out.println("// This would cause compilation error:");
        System.out.println("// Map.of(");
        System.out.println("//     \"productName\", \"Test Product\",    // String");
        System.out.println("//     \"quantitySold\", 42,                // Integer");
        System.out.println("//     \"revenue\", BigDecimal.valueOf(1000) // BigDecimal");
        System.out.println("// )");
        System.out.println("// Error: incompatible types: inference variable T has incompatible bounds");
    }
}
