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
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CategoryStatus status = CategoryStatus.ACTIVE;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "icon")
    private String icon;

    @Column(name = "color_code")
    private String colorCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products;

    // Enums
    public enum CategoryStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }

    // Custom constructor for basic category creation
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = CategoryStatus.ACTIVE;
        this.displayOrder = 0;
    }

    // Business logic methods
    public boolean isActive() {
        return status == CategoryStatus.ACTIVE;
    }

    public int getProductCount() {
        return products != null ? products.size() : 0;
    }
}
