package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor // Add this missing annotation
@Builder
@ToString // Remove exclude parameter
@EqualsAndHashCode // Remove exclude parameter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    // New attributes for better customer management
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    @Builder.Default
    private CustomerType customerType = CustomerType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status")
    @Builder.Default
    private CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "preferred_payment_method")
    private String preferredPaymentMethod;

    @Column(name = "credit_limit", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "website")
    private String website;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;

    @Column(name = "total_purchases", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified")
    @Builder.Default
    private Boolean isPhoneVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // Keep field-level annotation
    @EqualsAndHashCode.Exclude // Keep field-level annotation
    private List<Sale> sales;

    // Custom constructors for specific use cases
    public Customer(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.customerType = CustomerType.REGULAR;
        this.customerStatus = CustomerStatus.ACTIVE;
        this.creditLimit = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.loyaltyPoints = 0;
        this.totalPurchases = BigDecimal.ZERO;
        this.isEmailVerified = false;
        this.isPhoneVerified = false;
    }

    public Customer(String firstName, String lastName, String email, String phone,
                   String billingAddress, String shippingAddress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = firstName + " " + lastName;
        this.email = email;
        this.phone = phone;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.address = billingAddress; // Default to billing address
        this.customerType = CustomerType.REGULAR;
        this.customerStatus = CustomerStatus.ACTIVE;
        this.creditLimit = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.loyaltyPoints = 0;
        this.totalPurchases = BigDecimal.ZERO;
        this.isEmailVerified = false;
        this.isPhoneVerified = false;
    }

    // Enums
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum CustomerType {
        REGULAR, VIP, PREMIUM, CORPORATE, WHOLESALE
    }

    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED, BLACKLISTED
    }

    // Utility methods (business logic methods)
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return name;
    }

    public boolean hasAvailableCredit(BigDecimal amount) {
        return creditLimit.subtract(currentBalance).compareTo(amount) >= 0;
    }

    public void addLoyaltyPoints(Integer points) {
        this.loyaltyPoints = (this.loyaltyPoints != null ? this.loyaltyPoints : 0) + points;
    }

    public void updateTotalPurchases(BigDecimal amount) {
        this.totalPurchases = this.totalPurchases.add(amount);
        this.lastPurchaseDate = LocalDateTime.now();
    }
}
