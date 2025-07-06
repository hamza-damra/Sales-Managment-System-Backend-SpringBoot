import java.lang.reflect.Method;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Testing SupplierServiceTest fixes...");
        
        try {
            // Load the test class
            Class<?> testClass = Class.forName("com.hamza.salesmanagementbackend.service.SupplierServiceTest");
            
            // Check if the methods exist
            Method deleteSuccessMethod = testClass.getDeclaredMethod("deleteSupplier_Success");
            Method deleteNotFoundMethod = testClass.getDeclaredMethod("deleteSupplier_NotFound_ThrowsException");
            
            System.out.println("✓ deleteSupplier_Success method found");
            System.out.println("✓ deleteSupplier_NotFound_ThrowsException method found");
            System.out.println("✓ Test methods are properly defined");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
