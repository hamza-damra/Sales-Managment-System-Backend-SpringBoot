package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppliedPromotionRepositoryMinimalTest {

    @Autowired
    private AppliedPromotionRepository appliedPromotionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testRepositoryIsNotNull() {
        assertNotNull(appliedPromotionRepository);
        assertNotNull(entityManager);
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
