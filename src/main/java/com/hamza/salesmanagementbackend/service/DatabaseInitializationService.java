package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.User;
import com.hamza.salesmanagementbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

/**
 * Service to handle database initialization and ensure proper schema setup
 * Runs after application startup to verify database state and create default users
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early in the startup process
public class DatabaseInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database initialization...");
        
        try {
            // Verify database connection
            verifyDatabaseConnection();
            
            // Create default users if they don't exist
            createDefaultUsers();
            
            log.info("Database initialization completed successfully");
            
        } catch (Exception e) {
            log.error("Database initialization failed", e);
            // Don't throw exception to prevent application startup failure
            // Just log the error and continue
        }
    }

    private void verifyDatabaseConnection() {
        try {
            // Simple query to verify database connection
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            log.info("Database connection verified successfully");
        } catch (Exception e) {
            log.error("Database connection verification failed", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }

    private void createDefaultUsers() {
        log.info("Checking for default users...");
        
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            createUser("admin", "admin123", "admin@company.com",
                      "Admin", "User", User.Role.ADMIN);
            log.info("Created default admin user");
        }

        // Create manager user if not exists
        if (!userRepository.existsByUsername("manager")) {
            createUser("manager", "manager123", "manager@company.com",
                      "Manager", "User", User.Role.MANAGER);
            log.info("Created default manager user");
        }
        
        // Create other test users
        createTestUsersIfNotExist();
        
        log.info("Default users verification completed");
    }

    private void createTestUsersIfNotExist() {
        // Sales analyst
        if (!userRepository.existsByUsername("sales_analyst")) {
            createUser("sales_analyst", "sales123", "sales@company.com",
                      "Sales", "Analyst", User.Role.SALES_ANALYST);
        }

        // Financial analyst
        if (!userRepository.existsByUsername("financial_analyst")) {
            createUser("financial_analyst", "finance123", "finance@company.com",
                      "Financial", "Analyst", User.Role.FINANCIAL_ANALYST);
        }

        // Inventory analyst
        if (!userRepository.existsByUsername("inventory_analyst")) {
            createUser("inventory_analyst", "inventory123", "inventory@company.com",
                      "Inventory", "Analyst", User.Role.INVENTORY_ANALYST);
        }

        // Regular user
        if (!userRepository.existsByUsername("user")) {
            createUser("user", "user123", "user@company.com",
                      "Regular", "User", User.Role.USER);
        }
    }

    private void createUser(String username, String password, String email,
                           String firstName, String lastName, User.Role role) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
            log.info("Created user: {} with role: {}", username, role);

        } catch (Exception e) {
            log.error("Failed to create user: {}", username, e);
        }
    }
}
