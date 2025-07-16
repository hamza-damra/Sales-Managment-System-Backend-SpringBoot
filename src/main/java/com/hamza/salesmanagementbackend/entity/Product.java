package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @Column(unique = true)
    private String sku;

    // New comprehensive attributes
    @Column(name = "cost_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    private String brand;

    @Column(name = "model_number")
    private String modelNumber;

    @Column(unique = true)
    private String barcode;

    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight; // in kg

    @Column(name = "length", precision = 8, scale = 2)
    private BigDecimal length; // in cm

    @Column(name = "width", precision = 8, scale = 2)
    private BigDecimal width; // in cm

    @Column(name = "height", precision = 8, scale = 2)
    private BigDecimal height; // in cm

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status")
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 5;

    @Column(name = "max_stock_level")
    @Builder.Default
    private Integer maxStockLevel = 1000;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 10;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 20;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_code")
    private String supplierCode;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod; // in months

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    private String tags; // Comma-separated

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(
        name = "product_additional_images",
        joinColumns = @JoinColumn(name = "product_id"),
        indexes = @Index(name = "idx_product_images", columnList = "product_id, image_url")
    )
    @Column(name = "image_url")
    private List<String> additionalImages;

    @Column(name = "is_serialized")
    @Builder.Default
    private Boolean isSerialized = false;

    @Column(name = "is_digital")
    @Builder.Default
    private Boolean isDigital = false;

    @Column(name = "is_taxable")
    @Builder.Default
    private Boolean isTaxable = true;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("0.00");

    @Column(name = "unit_of_measure")
    private String unitOfMeasure; // e.g., PIECE, KG, LITER

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "location_in_warehouse")
    private String locationInWarehouse;

    @Column(name = "total_sold")
    @Builder.Default
    private Integer totalSold = 0;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "last_sold_date")
    private LocalDateTime lastSoldDate;

    @Column(name = "last_restocked_date")
    private LocalDateTime lastRestockedDate;

    @Lob
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<SaleItem> saleItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ReturnItem> returnItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

    // Enums
    public enum ProductStatus {
        ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK, COMING_SOON
    }

    // Custom constructors for specific use cases
    public Product(String name, String description, BigDecimal price, Integer stockQuantity, Category category, String sku) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.category = category;
        this.sku = sku;
        this.productStatus = ProductStatus.ACTIVE;
        this.costPrice = BigDecimal.ZERO;
        this.minStockLevel = 5;
        this.maxStockLevel = 1000;
        this.reorderPoint = 10;
        this.reorderQuantity = 20;
        this.isSerialized = false;
        this.isDigital = false;
        this.isTaxable = true;
        this.taxRate = BigDecimal.ZERO;
        this.unitOfMeasure = "PCS";
        this.discountPercentage = BigDecimal.ZERO;
        this.totalSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        // Initialize collections
        this.saleItems = new ArrayList<>();
        this.returnItems = new ArrayList<>();
        this.purchaseOrderItems = new ArrayList<>();
    }

    public Product(String name, String description, BigDecimal price, BigDecimal costPrice,
                  Integer stockQuantity, Category category, String sku, String brand) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.costPrice = costPrice != null ? costPrice : BigDecimal.ZERO;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.category = category;
        this.sku = sku;
        this.brand = brand;
        this.productStatus = ProductStatus.ACTIVE;
        this.minStockLevel = 5;
        this.maxStockLevel = 1000;
        this.reorderPoint = 10;
        this.reorderQuantity = 20;
        this.isSerialized = false;
        this.isDigital = false;
        this.isTaxable = true;
        this.taxRate = BigDecimal.ZERO;
        this.unitOfMeasure = "PCS";
        this.discountPercentage = BigDecimal.ZERO;
        this.totalSold = 0;
        this.totalRevenue = BigDecimal.ZERO;
        // Initialize collections
        this.saleItems = new ArrayList<>();
        this.returnItems = new ArrayList<>();
        this.purchaseOrderItems = new ArrayList<>();
    }

    // Business logic methods
    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public boolean needsReorder() {
        return stockQuantity <= reorderPoint;
    }

    public BigDecimal getMargin() {
        if (costPrice == null || costPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return price.subtract(costPrice);
    }

    public BigDecimal getMarginPercentage() {
        if (costPrice == null || costPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return getMargin().divide(costPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public BigDecimal getEffectivePrice() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.multiply(discountPercentage).divide(new BigDecimal("100"));
            return price.subtract(discount);
        }
        return price;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public void updateStock(int quantity) {
        this.stockQuantity += quantity;
        if (quantity > 0) {
            this.lastRestockedDate = LocalDateTime.now();
        }
    }

    public void recordSale(int quantity, BigDecimal salePrice) {
        this.stockQuantity -= quantity;
        this.totalSold += quantity;
        this.totalRevenue = this.totalRevenue.add(salePrice.multiply(new BigDecimal(quantity)));
        this.lastSoldDate = LocalDateTime.now();
    }

    public void updateSalesStats(int quantity, BigDecimal revenue) {
        this.totalSold += quantity;
        this.totalRevenue = this.totalRevenue.add(revenue);
        this.lastSoldDate = LocalDateTime.now();

        // Update stock quantity
        this.stockQuantity -= quantity;
    }
}
