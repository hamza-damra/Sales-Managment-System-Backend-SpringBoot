package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.ProductDTO;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String category) {

        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10; // Limit max page size

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductDTO> products;
        if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category.trim(), pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
                                                   @Valid @RequestBody ProductDTO productDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(@PathVariable Long id,
                                                 @RequestBody Map<String, Integer> request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Integer newStockQuantity = request.get("stockQuantity");
        if (newStockQuantity == null) {
            return ResponseEntity.badRequest().build();
        }

        ProductDTO updatedProduct = productService.updateStock(id, newStockQuantity);
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping("/{id}/stock/increase")
    public ResponseEntity<ProductDTO> increaseStock(@PathVariable Long id,
                                                   @RequestBody Map<String, Integer> request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ProductDTO updatedProduct = productService.restockProduct(id, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/stock/reduce")
    public ResponseEntity<ProductDTO> reduceStock(@PathVariable Long id,
                                                 @RequestBody Map<String, Integer> request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            productService.reduceStock(id, quantity);
            ProductDTO updatedProduct = productService.getProductById(id);
            return ResponseEntity.ok(updatedProduct);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
