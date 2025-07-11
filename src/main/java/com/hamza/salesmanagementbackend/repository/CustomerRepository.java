package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.customer.id = :customerId")
    Long countSalesByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.customer.id = :customerId")
    Long countReturnsByCustomerId(@Param("customerId") Long customerId);

    // Soft delete queries
    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false OR c.isDeleted IS NULL")
    List<Customer> findAllActive();

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = false OR c.isDeleted IS NULL")
    Page<Customer> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    Optional<Customer> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM Customer c WHERE c.email = :email AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    Optional<Customer> findByEmailAndNotDeleted(@Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = true")
    List<Customer> findAllDeleted();

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = true")
    Page<Customer> findAllDeleted(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(c.isDeleted = false OR c.isDeleted IS NULL) AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> searchActiveCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE (c.isDeleted = false OR c.isDeleted IS NULL) AND c.name LIKE %:name%")
    List<Customer> findActiveByNameContainingIgnoreCase(@Param("name") String name);

    boolean existsByEmailAndIsDeletedFalse(String email);

    // Debug and maintenance queries
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isDeleted IS NULL")
    Long countCustomersWithNullIsDeleted();

    @Modifying
    @Query("UPDATE Customer c SET c.isDeleted = false WHERE c.isDeleted IS NULL")
    int fixNullIsDeletedValues();
}
