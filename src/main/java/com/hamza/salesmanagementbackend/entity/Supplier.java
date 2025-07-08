package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;

    private String country;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_terms")
    private String deliveryTerms;

    @Builder.Default
    private Double rating = 0.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "total_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PurchaseOrder> purchaseOrders;

    // Enums
    public enum SupplierStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    // Custom constructors for specific use cases
    public Supplier(String name, String contactPerson, String phone, String email, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = SupplierStatus.ACTIVE;
        this.rating = 0.0;
        this.totalOrders = 0;
        this.totalAmount = BigDecimal.ZERO;
    }

    // Business logic methods
    public void updateRating(Double newRating) {
        if (newRating != null && newRating >= 0.0 && newRating <= 5.0) {
            this.rating = newRating;
        }
    }

    public void addOrder(BigDecimal orderAmount) {
        this.totalOrders = (this.totalOrders != null ? this.totalOrders : 0) + 1;
        this.totalAmount = this.totalAmount.add(orderAmount);
        this.lastOrderDate = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            fullAddress.append(address);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country);
        }
        return fullAddress.toString();
    }
}
