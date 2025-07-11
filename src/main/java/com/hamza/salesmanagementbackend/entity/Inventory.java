package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    @Column(name = "capacity")
    private Integer capacity; // Maximum number of items/products

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

    @Column(name = "operating_hours")
    private String operatingHours;

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

    public boolean hasCapacityFor(int additionalItems) {
        if (capacity == null) {
            return true; // No capacity limit set
        }
        return (currentStockCount + additionalItems) <= capacity;
    }

    public void updateStockCount(int newCount) {
        this.currentStockCount = Math.max(0, newCount);
    }

    public double getCapacityUtilization() {
        if (capacity == null || capacity == 0) {
            return 0.0;
        }
        return (double) currentStockCount / capacity * 100;
    }

    public boolean isNearCapacity(double threshold) {
        return getCapacityUtilization() >= threshold;
    }
}
