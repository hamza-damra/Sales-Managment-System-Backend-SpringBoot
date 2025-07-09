package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=false",
    "logging.level.org.hibernate.SQL=DEBUG"
})
class AppliedPromotionRepositoryMinimalTest {

    @Autowired
    private AppliedPromotionRepository appliedPromotionRepository;

    @Test
    void testRepositoryIsNotNull() {
        assertNotNull(appliedPromotionRepository);
    }

    @Test
    void testBasicRepositoryOperations() {
        // Test that we can call basic repository methods without errors
        long count = appliedPromotionRepository.count();
        assertTrue(count >= 0);
        
        // Test findAll doesn't throw exception
        assertDoesNotThrow(() -> appliedPromotionRepository.findAll());
    }
}
