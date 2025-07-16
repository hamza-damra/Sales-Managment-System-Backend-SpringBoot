package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.InventoryDTO;
import com.hamza.salesmanagementbackend.entity.Inventory;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.InventoryService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventories")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<Page<InventoryDTO>> getAllInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createInventorySort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<InventoryDTO> inventories = inventoryService.getAllInventories(pageable);
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/active")
    public ResponseEntity<List<InventoryDTO>> getAllActiveInventories() {
        List<InventoryDTO> inventories = inventoryService.getAllActiveInventories();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/main-warehouses")
    public ResponseEntity<List<InventoryDTO>> getMainWarehouses() {
        List<InventoryDTO> inventories = inventoryService.getMainWarehouses();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InventoryDTO>> searchInventories(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createInventorySort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<InventoryDTO> inventories = inventoryService.searchInventories(query.trim(), pageable);
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InventoryDTO inventory = inventoryService.getInventoryById(id);
            return ResponseEntity.ok(inventory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<InventoryDTO> getInventoryByName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InventoryDTO inventory = inventoryService.getInventoryByName(name.trim());
            return ResponseEntity.ok(inventory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/warehouse-code/{warehouseCode}")
    public ResponseEntity<InventoryDTO> getInventoryByWarehouseCode(@PathVariable String warehouseCode) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InventoryDTO inventory = inventoryService.getInventoryByWarehouseCode(warehouseCode.trim());
            return ResponseEntity.ok(inventory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO createdInventory = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long id,
                                                       @Valid @RequestBody InventoryDTO inventoryDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryDTO);
            return ResponseEntity.ok(updatedInventory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InventoryDTO>> getInventoriesByStatus(@PathVariable String status) {
        try {
            Inventory.InventoryStatus inventoryStatus = Inventory.InventoryStatus.valueOf(status.toUpperCase());
            List<InventoryDTO> inventories = inventoryService.getInventoriesByStatus(inventoryStatus);
            return ResponseEntity.ok(inventories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/empty")
    public ResponseEntity<List<InventoryDTO>> getEmptyInventories() {
        List<InventoryDTO> inventories = inventoryService.getEmptyInventories();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/with-dimensions")
    public ResponseEntity<List<InventoryDTO>> getInventoriesWithDimensions() {
        List<InventoryDTO> inventories = inventoryService.getInventoriesWithDimensions();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/without-dimensions")
    public ResponseEntity<List<InventoryDTO>> getInventoriesWithoutDimensions() {
        List<InventoryDTO> inventories = inventoryService.getInventoriesWithoutDimensions();
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InventoryDTO> updateInventoryStatus(@PathVariable Long id,
                                                             @RequestBody Map<String, String> request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String statusStr = request.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Inventory.InventoryStatus status = Inventory.InventoryStatus.valueOf(statusStr.toUpperCase());
            InventoryDTO updatedInventory = inventoryService.updateInventoryStatus(id, status);
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
