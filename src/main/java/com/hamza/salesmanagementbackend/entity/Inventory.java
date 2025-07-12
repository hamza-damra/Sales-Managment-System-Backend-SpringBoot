package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "inventories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Inventory name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_phone")
    private String managerPhone;

    @Column(name = "manager_email")
    private String managerEmail;

    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Length must have at most 10 integer digits and 2 decimal places")
    @Column(name = "length", precision = 12, scale = 2)
    private BigDecimal length; // Length in meters

    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Width must have at most 10 integer digits and 2 decimal places")
    @Column(name = "width", precision = 12, scale = 2)
    private BigDecimal width; // Width in meters

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Height must have at most 10 integer digits and 2 decimal places")
    @Column(name = "height", precision = 12, scale = 2)
    private BigDecimal height; // Height in meters

    @Column(name = "current_stock_count")
    @Builder.Default
    private Integer currentStockCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private InventoryStatus status = InventoryStatus.ACTIVE;

    @Column(name = "warehouse_code", unique = true)
    private String warehouseCode;

    @Column(name = "is_main_warehouse")
    @Builder.Default
    private Boolean isMainWarehouse = false;

    @Column(name = "start_work_time")
    private LocalTime startWorkTime; // Daily opening/start time

    @Column(name = "end_work_time")
    private LocalTime endWorkTime; // Daily closing/end time

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "inventory", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Category> categories;

    // Enums
    public enum InventoryStatus {
        ACTIVE, INACTIVE, ARCHIVED, MAINTENANCE
    }

    // Custom constructor for basic inventory creation
    public Inventory(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.status = InventoryStatus.ACTIVE;
        this.currentStockCount = 0;
        this.isMainWarehouse = false;
    }

    // Custom constructor with manager details
    public Inventory(String name, String description, String location, String managerName, String managerPhone) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.status = InventoryStatus.ACTIVE;
        this.currentStockCount = 0;
        this.isMainWarehouse = false;
    }

    // Business logic methods
    public boolean isActive() {
        return status == InventoryStatus.ACTIVE;
    }

    public boolean isMainWarehouse() {
        return Boolean.TRUE.equals(isMainWarehouse);
    }

    public int getCategoryCount() {
        return categories != null ? categories.size() : 0;
    }

    public void updateStockCount(int newCount) {
        this.currentStockCount = Math.max(0, newCount);
    }

    /**
     * Calculate the volume of the inventory space in cubic meters
     * @return volume in cubic meters, or null if any dimension is missing
     */
    public BigDecimal getVolume() {
        if (length == null || width == null || height == null) {
            return null;
        }
        return length.multiply(width).multiply(height);
    }

    /**
     * Calculate the floor area of the inventory space in square meters
     * @return floor area in square meters, or null if length or width is missing
     */
    public BigDecimal getFloorArea() {
        if (length == null || width == null) {
            return null;
        }
        return length.multiply(width);
    }

    /**
     * Check if all physical dimensions are set
     * @return true if length, width, and height are all set
     */
    public boolean hasDimensions() {
        return length != null && width != null && height != null;
    }

    /**
     * Check if both work times are set
     * @return true if both start and end work times are set
     */
    public boolean hasWorkTimes() {
        return startWorkTime != null && endWorkTime != null;
    }

    /**
     * Validate that start work time is before end work time
     * @return true if work times are valid (start before end), false if invalid or either is null
     */
    public boolean isWorkTimeValid() {
        if (startWorkTime == null || endWorkTime == null) {
            return true; // Allow null values, validation will be handled by business logic
        }
        return startWorkTime.isBefore(endWorkTime);
    }

    /**
     * Get the duration of work hours in minutes
     * @return duration in minutes, or null if either time is missing
     */
    public Long getWorkDurationMinutes() {
        if (startWorkTime == null || endWorkTime == null) {
            return null;
        }
        return java.time.Duration.between(startWorkTime, endWorkTime).toMinutes();
    }
}
