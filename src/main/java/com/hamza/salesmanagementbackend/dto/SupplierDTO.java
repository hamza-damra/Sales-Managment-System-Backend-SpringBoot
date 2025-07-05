package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Supplier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactPerson;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String address;

    private String city;

    private String country;

    private String taxNumber;

    private String paymentTerms;

    private String deliveryTerms;

    private Double rating;

    private Supplier.SupplierStatus status;

    private Integer totalOrders;

    private BigDecimal totalAmount;

    private LocalDateTime lastOrderDate;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Computed fields for frontend
    private String fullAddress;
    private Boolean isActive;
    private String statusDisplay;
    private String ratingDisplay;
}
