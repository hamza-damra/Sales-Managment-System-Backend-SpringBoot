package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Inventory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {

    private Long id;

    @NotBlank(message = "Inventory name is required")
    private String name;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private String address;

    private String managerName;

    private String managerPhone;

    private String managerEmail;

    private Integer capacity;

    private Integer currentStockCount;

    private Inventory.InventoryStatus status;

    private String warehouseCode;

    private Boolean isMainWarehouse;

    private String operatingHours;

    private String contactPhone;

    private String contactEmail;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Additional fields for API responses
    private Integer categoryCount;

    private Double capacityUtilization;

    private Boolean isNearCapacity;

    // Constructor for basic inventory creation
    public InventoryDTO(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.status = Inventory.InventoryStatus.ACTIVE;
        this.currentStockCount = 0;
        this.isMainWarehouse = false;
    }

    // Constructor with manager details
    public InventoryDTO(String name, String description, String location, String managerName, String managerPhone) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.status = Inventory.InventoryStatus.ACTIVE;
        this.currentStockCount = 0;
        this.isMainWarehouse = false;
    }
}
