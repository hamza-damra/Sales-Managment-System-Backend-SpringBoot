package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CustomerDTO;
import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;


    /**
     * Creates a new customer after validating email uniqueness
     */
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating new customer with email: {}", customerDTO.getEmail());
        validateEmailUniqueness(customerDTO.getEmail(), null);
        Customer customer = mapToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Successfully created customer with ID: {}", savedCustomer.getId());
        return mapToDTO(savedCustomer);
    }

    /**
     * Retrieves all active customers and converts them to DTOs using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAllActive()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all active customers with pagination: page={}, size={}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<CustomerDTO> result = customerRepository.findAllActive(pageable)
                .map(this::mapToDTO);
        log.debug("Found {} active customers out of {} total",
                 result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    /**
     * Debug method: Retrieves ALL customers regardless of deletion status
     * This method should only be used for debugging purposes
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomersIncludingDeleted(Pageable pageable) {
        log.debug("Fetching ALL customers (including deleted) with pagination: page={}, size={}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<CustomerDTO> result = customerRepository.findAll(pageable)
                .map(this::mapToDTO);
        log.debug("Found {} customers total", result.getTotalElements());
        return result;
    }

    /**
     * Debug method: Fix customers with NULL isDeleted values
     * This method should only be used for maintenance purposes
     */
    @Transactional
    public int fixCustomersWithNullIsDeleted() {
        Long count = customerRepository.countCustomersWithNullIsDeleted();
        log.info("Found {} customers with NULL isDeleted values", count);

        if (count > 0) {
            int fixed = customerRepository.fixNullIsDeletedValues();
            log.info("Fixed {} customers with NULL isDeleted values", fixed);
            return fixed;
        }
        return 0;
    }

    /**
     * Retrieves an active customer by ID with proper error handling
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findByIdAndNotDeleted(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    /**
     * Updates customer information with validation
     */
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        validateEmailUniqueness(customerDTO.getEmail(), id);
        updateCustomerFields(existingCustomer, customerDTO);
        Customer savedCustomer = customerRepository.save(existingCustomer);
        return mapToDTO(savedCustomer);
    }

    /**
     * Deletes a customer by ID (defaults to soft delete)
     */
    public void deleteCustomer(Long id) {
        softDeleteCustomer(id, "SYSTEM", "Customer deletion requested");
    }

    /**
     * Deletes a customer by ID with optional force deletion
     * @param id Customer ID to delete
     * @param forceDelete If true, allows cascade deletion of related records
     */
    public void deleteCustomer(Long id, boolean forceDelete) {
        if (forceDelete) {
            hardDeleteCustomer(id);
        } else {
            softDeleteCustomer(id, "SYSTEM", "Customer deletion requested");
        }
    }

    /**
     * Soft deletes a customer (recommended approach)
     * @param id Customer ID to soft delete
     * @param deletedBy User who initiated the deletion
     * @param reason Reason for deletion
     */
    public void softDeleteCustomer(Long id, String deletedBy, String reason) {
        Customer customer = customerRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.canBeDeleted()) {
            throw new BusinessLogicException("Customer cannot be deleted due to current status: " + customer.getCustomerStatus());
        }

        customer.softDelete(deletedBy, reason);
        customerRepository.save(customer);

        log.info("Soft deleted customer {} by {} with reason: {}", id, deletedBy, reason);
    }

    /**
     * Hard deletes a customer with cascade deletion of related records
     * @param id Customer ID to hard delete
     */
    public void hardDeleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Log cascade deletion for audit purposes
        Long salesCount = customerRepository.countSalesByCustomerId(id);
        Long returnCount = customerRepository.countReturnsByCustomerId(id);

        if (salesCount > 0 || returnCount > 0) {
            log.warn("Hard deleting customer {} with {} sales and {} returns - cascade deletion will occur",
                    id, salesCount, returnCount);
        }

        customerRepository.deleteById(id);
        log.info("Hard deleted customer {} with {} sales and {} returns", id, salesCount, returnCount);
    }

    /**
     * Restores a soft-deleted customer
     * @param id Customer ID to restore
     */
    public CustomerDTO restoreCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.isDeleted()) {
            throw new BusinessLogicException("Customer is not deleted and cannot be restored");
        }

        customer.restore();
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Restored customer {}", id);
        return mapToDTO(savedCustomer);
    }

    /**
     * Searches active customers by name using streams for filtering
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomersByName(String name) {
        return customerRepository.findActiveByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Advanced search with pagination (active customers only)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> searchCustomers(String searchTerm, Pageable pageable) {
        return customerRepository.searchActiveCustomers(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Finds active customer by email
     */
    @Transactional(readOnly = true)
    public Optional<CustomerDTO> findByEmail(String email) {
        return customerRepository.findByEmailAndNotDeleted(email)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves all deleted customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getDeletedCustomers(Pageable pageable) {
        return customerRepository.findAllDeleted(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets customers created within a date range using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return customerRepository.findByCreatedAtBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Gets count of new customers since a specific date
     */
    @Transactional(readOnly = true)
    public Long getNewCustomersCount(LocalDateTime since) {
        return customerRepository.countNewCustomersSince(since);
    }

    /**
     * Gets customer with sales information
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerWithSales(Long id) {
        return customerRepository.findByIdWithSales(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    // Private helper methods

    private void validateEmailUniqueness(String email, Long excludeId) {
        if (email != null && !email.trim().isEmpty()) {
            customerRepository.findByEmailAndNotDeleted(email)
                    .filter(customer -> excludeId == null || !customer.getId().equals(excludeId))
                    .ifPresent(customer -> {
                        throw new BusinessLogicException("Email already exists: " + email);
                    });
        }
    }

    private void updateCustomerFields(Customer existingCustomer, CustomerDTO customerDTO) {
        Optional.ofNullable(customerDTO.getName()).ifPresent(existingCustomer::setName);
        Optional.ofNullable(customerDTO.getFirstName()).ifPresent(existingCustomer::setFirstName);
        Optional.ofNullable(customerDTO.getLastName()).ifPresent(existingCustomer::setLastName);
        Optional.ofNullable(customerDTO.getEmail()).ifPresent(existingCustomer::setEmail);
        Optional.ofNullable(customerDTO.getPhone()).ifPresent(existingCustomer::setPhone);
        Optional.ofNullable(customerDTO.getAddress()).ifPresent(existingCustomer::setAddress);
        Optional.ofNullable(customerDTO.getBillingAddress()).ifPresent(existingCustomer::setBillingAddress);
        Optional.ofNullable(customerDTO.getShippingAddress()).ifPresent(existingCustomer::setShippingAddress);
        Optional.ofNullable(customerDTO.getDateOfBirth()).ifPresent(existingCustomer::setDateOfBirth);
        Optional.ofNullable(customerDTO.getGender()).ifPresent(existingCustomer::setGender);
        Optional.ofNullable(customerDTO.getCustomerType()).ifPresent(existingCustomer::setCustomerType);
        Optional.ofNullable(customerDTO.getCustomerStatus()).ifPresent(existingCustomer::setCustomerStatus);
        Optional.ofNullable(customerDTO.getPreferredPaymentMethod()).ifPresent(existingCustomer::setPreferredPaymentMethod);
        Optional.ofNullable(customerDTO.getCreditLimit()).ifPresent(existingCustomer::setCreditLimit);
        Optional.ofNullable(customerDTO.getTaxNumber()).ifPresent(existingCustomer::setTaxNumber);
        Optional.ofNullable(customerDTO.getCompanyName()).ifPresent(existingCustomer::setCompanyName);
        Optional.ofNullable(customerDTO.getWebsite()).ifPresent(existingCustomer::setWebsite);
        Optional.ofNullable(customerDTO.getNotes()).ifPresent(existingCustomer::setNotes);

        // Update name if first/last name provided
        if (customerDTO.getFirstName() != null && customerDTO.getLastName() != null) {
            existingCustomer.setName(customerDTO.getFirstName() + " " + customerDTO.getLastName());
        }
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        BeanUtils.copyProperties(customer, dto);
        return dto;
    }

    private Customer mapToEntity(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer, "id", "createdAt", "updatedAt");

        // Set defaults for null values
        if (customer.getCustomerType() == null) {
            customer.setCustomerType(Customer.CustomerType.REGULAR);
        }
        if (customer.getCustomerStatus() == null) {
            customer.setCustomerStatus(Customer.CustomerStatus.ACTIVE);
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }

        // Set name from first/last if provided
        if (customerDTO.getFirstName() != null && customerDTO.getLastName() != null) {
            customer.setName(customerDTO.getFirstName() + " " + customerDTO.getLastName());
        }

        return customer;
    }

    // New methods for enhanced customer management

    /**
     * Updates customer status
     */
    public CustomerDTO updateCustomerStatus(Long id, Customer.CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setCustomerStatus(status);
        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    /**
     * Updates customer type
     */
    public CustomerDTO updateCustomerType(Long id, Customer.CustomerType type) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setCustomerType(type);
        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    /**
     * Updates customer credit limit
     */
    public CustomerDTO updateCreditLimit(Long id, BigDecimal creditLimit) {
        if (creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessLogicException("Credit limit cannot be negative");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setCreditLimit(creditLimit);
        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    /**
     * Adds loyalty points to customer
     */
    public CustomerDTO addLoyaltyPoints(Long id, Integer points) {
        if (points <= 0) {
            throw new BusinessLogicException("Points must be greater than zero");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.addLoyaltyPoints(points);
        Customer savedCustomer = customerRepository.save(customer);
        return mapToDTO(savedCustomer);
    }

    /**
     * Gets customers by type using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByType(Customer.CustomerType type) {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getCustomerType() == type)
                .map(this::mapToDTO)
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets customers by status using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersByStatus(Customer.CustomerStatus status) {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getCustomerStatus() == status)
                .map(this::mapToDTO)
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets VIP customers with high loyalty points using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getVipCustomers() {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getCustomerType() == Customer.CustomerType.VIP ||
                                  customer.getLoyaltyPoints() >= 1000)
                .map(this::mapToDTO)
                .sorted((c1, c2) -> c2.getLoyaltyPoints().compareTo(c1.getLoyaltyPoints()))
                .collect(Collectors.toList());
    }

    /**
     * Gets customers with outstanding balance using streams
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomersWithOutstandingBalance() {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(this::mapToDTO)
                .sorted((c1, c2) -> c2.getCurrentBalance().compareTo(c1.getCurrentBalance()))
                .collect(Collectors.toList());
    }

    /**
     * Gets customer statistics using streams
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerStatistics() {
        List<Customer> allCustomers = customerRepository.findAll();

        return allCustomers.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        customers -> {
                            Map<Customer.CustomerType, Long> customersByType = customers.stream()
                                    .collect(Collectors.groupingBy(Customer::getCustomerType, Collectors.counting()));

                            Map<Customer.CustomerStatus, Long> customersByStatus = customers.stream()
                                    .collect(Collectors.groupingBy(Customer::getCustomerStatus, Collectors.counting()));

                            BigDecimal totalLoyaltyPoints = customers.stream()
                                    .map(Customer::getLoyaltyPoints)
                                    .map(points -> BigDecimal.valueOf(points != null ? points : 0))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal totalCreditLimit = customers.stream()
                                    .map(Customer::getCreditLimit)
                                    .filter(limit -> limit != null)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal totalOutstandingBalance = customers.stream()
                                    .map(Customer::getCurrentBalance)
                                    .filter(balance -> balance != null)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            long verifiedEmails = customers.stream()
                                    .mapToLong(c -> Boolean.TRUE.equals(c.getIsEmailVerified()) ? 1 : 0)
                                    .sum();

                            return Map.of(
                                    "totalCustomers", customers.size(),
                                    "customersByType", customersByType,
                                    "customersByStatus", customersByStatus,
                                    "totalLoyaltyPoints", totalLoyaltyPoints,
                                    "totalCreditLimit", totalCreditLimit,
                                    "totalOutstandingBalance", totalOutstandingBalance,
                                    "verifiedEmailsCount", verifiedEmails,
                                    "verificationRate", customers.isEmpty() ? 0 :
                                            BigDecimal.valueOf(verifiedEmails * 100.0 / customers.size())
                            );
                        }
                ));
    }
}
