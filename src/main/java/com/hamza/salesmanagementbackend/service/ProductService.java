package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ProductDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private static final Integer LOW_STOCK_THRESHOLD = 10;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new product after validating SKU uniqueness
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        validateSkuUniqueness(productDTO.getSku(), null);
        validateProductData(productDTO);
        Product product = mapToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Retrieves all products and converts them to DTOs using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a product by ID with proper error handling
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Updates product information with validation
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        validateSkuUniqueness(productDTO.getSku(), id);
        validateProductData(productDTO);
        updateProductFields(existingProduct, productDTO);
        Product savedProduct = productRepository.save(existingProduct);
        return mapToDTO(savedProduct);
    }

    /**
     * Deletes a product by ID
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        // Check for associated sale items
        Long saleItemCount = productRepository.countSaleItemsByProductId(id);
        if (saleItemCount > 0) {
            throw DataIntegrityException.productHasSaleItems(id, saleItemCount.intValue());
        }

        // Check for associated return items
        Long returnItemCount = productRepository.countReturnItemsByProductId(id);
        if (returnItemCount > 0) {
            throw DataIntegrityException.productHasReturnItems(id, returnItemCount.intValue());
        }

        productRepository.deleteById(id);
    }

    /**
     * Searches products using streams for filtering and sorting
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Advanced search with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.searchProducts(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets products by category ID using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets products by category ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets products by category name using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets products by category name with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategoryName(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryName(categoryName, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets products within price range using streams for additional filtering
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice)
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                .collect(Collectors.toList());
    }

    /**
     * Gets low stock products using streams for enhanced processing
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThan(LOW_STOCK_THRESHOLD)
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getStockQuantity().compareTo(p2.getStockQuantity()))
                .collect(Collectors.toList());
    }

    /**
     * Gets out of stock products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts()
                .stream()
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all distinct category names using streams
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategoryNames() {
        return productRepository.findDistinctCategoryNames()
                .stream()
                .filter(category -> category != null && !category.trim().isEmpty())
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    /**
     * Gets products grouped by category name using streams
     */
    @Transactional(readOnly = true)
    public Map<String, List<ProductDTO>> getProductsGroupedByCategoryName() {
        return productRepository.findAll()
                .stream()
                .filter(product -> product.getCategory() != null && product.getCategory().getName() != null)
                .collect(Collectors.groupingBy(
                        product -> product.getCategory().getName(),
                        Collectors.mapping(this::mapToDTO, Collectors.toList())
                ));
    }

    /**
     * Updates stock quantity for a product
     */
    public ProductDTO updateStock(Long productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new BusinessLogicException("Stock quantity cannot be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStockQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Reduces stock quantity (used during sales)
     */
    public void reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getName(), product.getStockQuantity(), quantity)
            );
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    /**
     * Finds product by SKU
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(this::mapToDTO);
    }

    /**
     * Gets inventory statistics using streams
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryStatistics() {
        List<Product> allProducts = productRepository.findAll();

        return allProducts.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        products -> Map.of(
                                "totalProducts", products.size(),
                                "totalValue", products.stream()
                                        .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                "lowStockCount", products.stream()
                                        .mapToInt(p -> p.getStockQuantity() < LOW_STOCK_THRESHOLD ? 1 : 0)
                                        .sum(),
                                "outOfStockCount", products.stream()
                                        .mapToInt(p -> p.getStockQuantity() == 0 ? 1 : 0)
                                        .sum(),
                                "averagePrice", products.stream()
                                        .map(Product::getPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(products.size()), RoundingMode.HALF_UP)
                        )
                ));
    }

    // Private helper methods

    private void validateSkuUniqueness(String sku, Long excludeId) {
        if (sku != null && !sku.trim().isEmpty()) {
            productRepository.findBySku(sku)
                    .filter(product -> excludeId == null || !product.getId().equals(excludeId))
                    .ifPresent(product -> {
                        throw new BusinessLogicException("SKU already exists: " + sku);
                    });
        }
    }

    private void validateBarcodeUniqueness(String barcode, Long excludeId) {
        if (barcode != null && !barcode.trim().isEmpty()) {
            productRepository.findAll().stream()
                    .filter(product -> barcode.equals(product.getBarcode()))
                    .filter(product -> excludeId == null || !product.getId().equals(excludeId))
                    .findFirst()
                    .ifPresent(product -> {
                        throw new BusinessLogicException("Barcode already exists: " + barcode);
                    });
        }
    }

    private void validateProductData(ProductDTO productDTO) {
        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("Product price must be greater than zero");
        }
        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() < 0) {
            throw new BusinessLogicException("Stock quantity cannot be negative");
        }
        if (productDTO.getCostPrice() != null && productDTO.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessLogicException("Cost price cannot be negative");
        }
        if (productDTO.getMinStockLevel() != null && productDTO.getMinStockLevel() < 0) {
            throw new BusinessLogicException("Minimum stock level cannot be negative");
        }
        if (productDTO.getReorderPoint() != null && productDTO.getReorderPoint() < 0) {
            throw new BusinessLogicException("Reorder point cannot be negative");
        }
        validateBarcodeUniqueness(productDTO.getBarcode(), productDTO.getId());
    }

    private void updateProductFields(Product existingProduct, ProductDTO productDTO) {
        Optional.ofNullable(productDTO.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(productDTO.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(productDTO.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(productDTO.getCostPrice()).ifPresent(existingProduct::setCostPrice);
        Optional.ofNullable(productDTO.getStockQuantity()).ifPresent(existingProduct::setStockQuantity);

        // Handle category update
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().trim().isEmpty()) {
            Category category = categoryRepository.findByNameIgnoreCase(productDTO.getCategoryName().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + productDTO.getCategoryName()));
            existingProduct.setCategory(category);
        }

        Optional.ofNullable(productDTO.getSku()).ifPresent(existingProduct::setSku);
        Optional.ofNullable(productDTO.getBrand()).ifPresent(existingProduct::setBrand);
        Optional.ofNullable(productDTO.getModelNumber()).ifPresent(existingProduct::setModelNumber);
        Optional.ofNullable(productDTO.getBarcode()).ifPresent(existingProduct::setBarcode);
        Optional.ofNullable(productDTO.getWeight()).ifPresent(existingProduct::setWeight);
        Optional.ofNullable(productDTO.getLength()).ifPresent(existingProduct::setLength);
        Optional.ofNullable(productDTO.getWidth()).ifPresent(existingProduct::setWidth);
        Optional.ofNullable(productDTO.getHeight()).ifPresent(existingProduct::setHeight);
        Optional.ofNullable(productDTO.getProductStatus()).ifPresent(existingProduct::setProductStatus);
        Optional.ofNullable(productDTO.getMinStockLevel()).ifPresent(existingProduct::setMinStockLevel);
        Optional.ofNullable(productDTO.getMaxStockLevel()).ifPresent(existingProduct::setMaxStockLevel);
        Optional.ofNullable(productDTO.getReorderPoint()).ifPresent(existingProduct::setReorderPoint);
        Optional.ofNullable(productDTO.getReorderQuantity()).ifPresent(existingProduct::setReorderQuantity);
        Optional.ofNullable(productDTO.getSupplierName()).ifPresent(existingProduct::setSupplierName);
        Optional.ofNullable(productDTO.getSupplierCode()).ifPresent(existingProduct::setSupplierCode);
        Optional.ofNullable(productDTO.getWarrantyPeriod()).ifPresent(existingProduct::setWarrantyPeriod);
        Optional.ofNullable(productDTO.getExpiryDate()).ifPresent(existingProduct::setExpiryDate);
        Optional.ofNullable(productDTO.getManufacturingDate()).ifPresent(existingProduct::setManufacturingDate);
        Optional.ofNullable(productDTO.getTags()).ifPresent(tags -> existingProduct.setTags(String.join(",", tags)));
        Optional.ofNullable(productDTO.getAdditionalImages()).ifPresent(images -> existingProduct.setAdditionalImages(new java.util.ArrayList<>(images)));
        Optional.ofNullable(productDTO.getIsSerialized()).ifPresent(existingProduct::setIsSerialized);
        Optional.ofNullable(productDTO.getIsDigital()).ifPresent(existingProduct::setIsDigital);
        Optional.ofNullable(productDTO.getIsTaxable()).ifPresent(existingProduct::setIsTaxable);
        Optional.ofNullable(productDTO.getTaxRate()).ifPresent(existingProduct::setTaxRate);
        Optional.ofNullable(productDTO.getUnitOfMeasure()).ifPresent(existingProduct::setUnitOfMeasure);
        Optional.ofNullable(productDTO.getDiscountPercentage()).ifPresent(existingProduct::setDiscountPercentage);
        Optional.ofNullable(productDTO.getLocationInWarehouse()).ifPresent(existingProduct::setLocationInWarehouse);
        Optional.ofNullable(productDTO.getNotes()).ifPresent(existingProduct::setNotes);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setStockQuantity(product.getStockQuantity());

        // Map category information
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        dto.setSku(product.getSku());
        dto.setBrand(product.getBrand());
        dto.setModelNumber(product.getModelNumber());
        dto.setBarcode(product.getBarcode());
        dto.setWeight(product.getWeight());
        dto.setLength(product.getLength());
        dto.setWidth(product.getWidth());
        dto.setHeight(product.getHeight());
        dto.setProductStatus(product.getProductStatus());
        dto.setMinStockLevel(product.getMinStockLevel());
        dto.setMaxStockLevel(product.getMaxStockLevel());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setReorderQuantity(product.getReorderQuantity());
        dto.setSupplierName(product.getSupplierName());
        dto.setSupplierCode(product.getSupplierCode());
        dto.setWarrantyPeriod(product.getWarrantyPeriod());
        dto.setExpiryDate(product.getExpiryDate());
        dto.setManufacturingDate(product.getManufacturingDate());
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(new java.util.HashSet<>(java.util.Arrays.asList(product.getTags().split(","))));
        }
        if (product.getAdditionalImages() != null) {
            dto.setAdditionalImages(new java.util.HashSet<>(product.getAdditionalImages()));
        }
        dto.setIsSerialized(product.getIsSerialized());
        dto.setIsDigital(product.getIsDigital());
        dto.setIsTaxable(product.getIsTaxable());
        dto.setTaxRate(product.getTaxRate());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setDiscountPercentage(product.getDiscountPercentage());
        dto.setLocationInWarehouse(product.getLocationInWarehouse());
        dto.setTotalSold(product.getTotalSold());
        dto.setTotalRevenue(product.getTotalRevenue());
        dto.setLastSoldDate(product.getLastSoldDate());
        dto.setLastRestockedDate(product.getLastRestockedDate());
        dto.setNotes(product.getNotes());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    private Product mapToEntity(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCostPrice(productDTO.getCostPrice() != null ? productDTO.getCostPrice() : BigDecimal.ZERO);
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);

        // Map category information
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        } else if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().trim().isEmpty()) {
            Category category = categoryRepository.findByNameIgnoreCase(productDTO.getCategoryName().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + productDTO.getCategoryName()));
            product.setCategory(category);
        }

        product.setSku(productDTO.getSku());
        product.setBrand(productDTO.getBrand());
        product.setModelNumber(productDTO.getModelNumber());
        product.setBarcode(productDTO.getBarcode());
        product.setWeight(productDTO.getWeight());
        product.setLength(productDTO.getLength());
        product.setWidth(productDTO.getWidth());
        product.setHeight(productDTO.getHeight());
        product.setProductStatus(productDTO.getProductStatus() != null ? productDTO.getProductStatus() : Product.ProductStatus.ACTIVE);
        product.setMinStockLevel(productDTO.getMinStockLevel() != null ? productDTO.getMinStockLevel() : 5);
        product.setMaxStockLevel(productDTO.getMaxStockLevel() != null ? productDTO.getMaxStockLevel() : 1000);
        product.setReorderPoint(productDTO.getReorderPoint() != null ? productDTO.getReorderPoint() : 10);
        product.setReorderQuantity(productDTO.getReorderQuantity() != null ? productDTO.getReorderQuantity() : 50);
        product.setSupplierName(productDTO.getSupplierName());
        product.setSupplierCode(productDTO.getSupplierCode());
        product.setWarrantyPeriod(productDTO.getWarrantyPeriod());
        product.setExpiryDate(productDTO.getExpiryDate());
        product.setManufacturingDate(productDTO.getManufacturingDate());
        if (productDTO.getTags() != null) {
            product.setTags(String.join(",", productDTO.getTags()));
        }
        if (productDTO.getAdditionalImages() != null) {
            product.setAdditionalImages(new java.util.ArrayList<>(productDTO.getAdditionalImages()));
        }
        product.setIsSerialized(productDTO.getIsSerialized() != null ? productDTO.getIsSerialized() : false);
        product.setIsDigital(productDTO.getIsDigital() != null ? productDTO.getIsDigital() : false);
        product.setIsTaxable(productDTO.getIsTaxable() != null ? productDTO.getIsTaxable() : true);
        product.setTaxRate(productDTO.getTaxRate() != null ? productDTO.getTaxRate() : BigDecimal.ZERO);
        product.setUnitOfMeasure(productDTO.getUnitOfMeasure() != null ? productDTO.getUnitOfMeasure() : "PCS");
        product.setDiscountPercentage(productDTO.getDiscountPercentage() != null ? productDTO.getDiscountPercentage() : BigDecimal.ZERO);
        product.setLocationInWarehouse(productDTO.getLocationInWarehouse());
        product.setNotes(productDTO.getNotes());
        return product;
    }

    // New methods for enhanced product management

    /**
     * Updates product status
     */
    public ProductDTO updateProductStatus(Long id, Product.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setProductStatus(status);
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Gets products by brand using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepository.findAll()
                .stream()
                .filter(product -> brand.equalsIgnoreCase(product.getBrand()))
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets products by supplier using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsBySupplier(String supplierName) {
        return productRepository.findAll()
                .stream()
                .filter(product -> supplierName.equalsIgnoreCase(product.getSupplierName()))
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets expired products using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getExpiredProducts() {
        return productRepository.findAll()
                .stream()
                .filter(Product::isExpired)
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getExpiryDate().compareTo(p2.getExpiryDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets products requiring reorder using streams
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsRequiringReorder() {
        return productRepository.findAll()
                .stream()
                .filter(Product::isLowStock)
                .map(this::mapToDTO)
                .sorted((p1, p2) -> p1.getStockQuantity().compareTo(p2.getStockQuantity()))
                .collect(Collectors.toList());
    }

    /**
     * Restocks a product
     */
    public ProductDTO restockProduct(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessLogicException("Restock quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        product.setLastRestockedDate(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Finds product by barcode
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findByBarcode(String barcode) {
        return productRepository.findAll()
                .stream()
                .filter(product -> barcode.equals(product.getBarcode()))
                .findFirst()
                .map(this::mapToDTO);
    }
}
