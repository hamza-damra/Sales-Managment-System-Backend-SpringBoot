package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByNameContainingIgnoreCase(String name);

    List<Customer> findByPhoneContaining(String phone);

    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.sales WHERE c.id = :id")
    Optional<Customer> findByIdWithSales(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :date")
    Long countNewCustomersSince(@Param("date") LocalDateTime date);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByEmail(String email);
}
