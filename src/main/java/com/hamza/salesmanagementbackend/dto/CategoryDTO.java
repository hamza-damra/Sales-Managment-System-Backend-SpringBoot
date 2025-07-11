package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Category;
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
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private Integer displayOrder;

    private Category.CategoryStatus status;

    private String imageUrl;

    private String icon;

    private String colorCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Inventory relationship
    private Long inventoryId;

    private String inventoryName;

    // Additional fields for API responses
    private Integer productCount;

    // Constructor for basic category creation
    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Category.CategoryStatus.ACTIVE;
        this.displayOrder = 0;
    }

    // Constructor with inventory
    public CategoryDTO(String name, String description, Long inventoryId) {
        this.name = name;
        this.description = description;
        this.inventoryId = inventoryId;
        this.status = Category.CategoryStatus.ACTIVE;
        this.displayOrder = 0;
    }
}
