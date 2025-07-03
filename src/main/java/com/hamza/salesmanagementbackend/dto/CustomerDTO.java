package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "Customer name is required")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    private String address;

    // Enhanced attributes matching the entity
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Customer.Gender gender;
    private Customer.CustomerType customerType;
    private Customer.CustomerStatus customerStatus;
    private String billingAddress;
    private String shippingAddress;
    private String preferredPaymentMethod;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private Integer loyaltyPoints;
    private String taxNumber;
    private String companyName;
    private String website;
    private String notes;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal totalPurchases;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
