package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Inventory;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Length must have at most 10 integer digits and 2 decimal places")
    private BigDecimal length; // Length in meters

    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Width must have at most 10 integer digits and 2 decimal places")
    private BigDecimal width; // Width in meters

    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Height must have at most 10 integer digits and 2 decimal places")
    private BigDecimal height; // Height in meters

    private Integer currentStockCount;

    private Inventory.InventoryStatus status;

    private String warehouseCode;

    private Boolean isMainWarehouse;

    private LocalTime startWorkTime;

    private LocalTime endWorkTime;

    private String contactPhone;

    private String contactEmail;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Additional fields for API responses
    private Integer categoryCount;

    private BigDecimal volume; // Calculated volume in cubic meters

    private BigDecimal floorArea; // Calculated floor area in square meters

    private Boolean hasDimensions; // Whether all dimensions are set

    private Boolean hasWorkTimes; // Whether both work times are set

    private Boolean isWorkTimeValid; // Whether start time is before end time

    private Long workDurationMinutes; // Duration of work hours in minutes

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
