package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.entity.User;
import com.hamza.salesmanagementbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default users with various roles for testing and development
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run before DataInitializer
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeUsers();
        }
    }

    private void initializeUsers() {
        log.info("Initializing default users...");

        // Admin user
        createUser("admin", "admin@company.com", "admin123", "Admin", "User", User.Role.ADMIN);
        
        // Manager user
        createUser("manager", "manager@company.com", "manager123", "Manager", "User", User.Role.MANAGER);
        
        // Sales Analyst
        createUser("sales_analyst", "sales@company.com", "sales123", "Sales", "Analyst", User.Role.SALES_ANALYST);
        
        // Financial Analyst
        createUser("financial_analyst", "finance@company.com", "finance123", "Financial", "Analyst", User.Role.FINANCIAL_ANALYST);
        
        // Inventory Analyst
        createUser("inventory_analyst", "inventory@company.com", "inventory123", "Inventory", "Analyst", User.Role.INVENTORY_ANALYST);
        
        // Customer Analyst
        createUser("customer_analyst", "customer@company.com", "customer123", "Customer", "Analyst", User.Role.CUSTOMER_ANALYST);
        
        // Marketing Analyst
        createUser("marketing_analyst", "marketing@company.com", "marketing123", "Marketing", "Analyst", User.Role.MARKETING_ANALYST);
        
        // Product Analyst
        createUser("product_analyst", "product@company.com", "product123", "Product", "Analyst", User.Role.PRODUCT_ANALYST);
        
        // Executive
        createUser("executive", "executive@company.com", "executive123", "Executive", "User", User.Role.EXECUTIVE);
        
        // Regular user
        createUser("user", "user@company.com", "user123", "Regular", "User", User.Role.USER);

        log.info("Successfully initialized {} users", userRepository.count());
        
        // Print credentials for testing
        log.info("\n=== TEST USER CREDENTIALS ===");
        log.info("Admin: admin / admin123");
        log.info("Manager: manager / manager123");
        log.info("Sales Analyst: sales_analyst / sales123");
        log.info("Financial Analyst: financial_analyst / finance123");
        log.info("Inventory Analyst: inventory_analyst / inventory123");
        log.info("Customer Analyst: customer_analyst / customer123");
        log.info("Marketing Analyst: marketing_analyst / marketing123");
        log.info("Product Analyst: product_analyst / product123");
        log.info("Executive: executive / executive123");
        log.info("User: user / user123");
        log.info("=============================");
    }

    private void createUser(String username, String email, String password, String firstName, String lastName, User.Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            
            userRepository.save(user);
            log.debug("Created user: {} with role: {}", username, role);
        }
    }
}
