package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.InventoryDTO;
import com.hamza.salesmanagementbackend.entity.Inventory;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Creates a new inventory after validating name uniqueness
     */
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        validateInventoryNameUniqueness(inventoryDTO.getName(), null);
        validateInventoryData(inventoryDTO);
        validateMainWarehouseConstraint(inventoryDTO, null);
        
        Inventory inventory = mapToEntity(inventoryDTO);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToDTO(savedInventory);
    }

    /**
     * Gets all inventories with pagination
     */
    @Transactional(readOnly = true)
    public Page<InventoryDTO> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets all active inventories
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getAllActiveInventories() {
        return inventoryRepository.findByStatus(Inventory.InventoryStatus.ACTIVE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets inventory by ID
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return mapToDTO(inventory);
    }

    /**
     * Gets inventory by name
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByName(String name) {
        Inventory inventory = inventoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with name: " + name));
        return mapToDTO(inventory);
    }

    /**
     * Gets inventory by warehouse code
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByWarehouseCode(String warehouseCode) {
        Inventory inventory = inventoryRepository.findByWarehouseCode(warehouseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with warehouse code: " + warehouseCode));
        return mapToDTO(inventory);
    }

    /**
     * Updates inventory information with validation
     */
    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) {
        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        validateInventoryNameUniqueness(inventoryDTO.getName(), id);
        validateInventoryData(inventoryDTO);
        validateMainWarehouseConstraint(inventoryDTO, id);
        
        updateInventoryFields(existingInventory, inventoryDTO);
        Inventory savedInventory = inventoryRepository.save(existingInventory);
        return mapToDTO(savedInventory);
    }

    /**
     * Deletes an inventory by ID
     */
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        // Check if inventory has categories
        Long categoryCount = inventoryRepository.countCategoriesByInventoryId(id);
        if (categoryCount > 0) {
            throw DataIntegrityException.inventoryHasCategories(id, categoryCount.intValue());
        }

        inventoryRepository.deleteById(id);
    }

    /**
     * Searches inventories with pagination
     */
    @Transactional(readOnly = true)
    public Page<InventoryDTO> searchInventories(String searchTerm, Pageable pageable) {
        return inventoryRepository.searchInventories(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets inventories by status
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoriesByStatus(Inventory.InventoryStatus status) {
        return inventoryRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets main warehouses
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getMainWarehouses() {
        return inventoryRepository.findMainWarehouses()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets empty inventories (inventories with no categories)
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getEmptyInventories() {
        return inventoryRepository.findEmptyInventories()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets inventories near capacity
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoriesNearCapacity(Double threshold) {
        return inventoryRepository.findInventoriesNearCapacity(threshold)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates inventory status
     */
    public InventoryDTO updateInventoryStatus(Long id, Inventory.InventoryStatus status) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        
        inventory.setStatus(status);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToDTO(savedInventory);
    }

    // Private validation and utility methods
    private void validateInventoryNameUniqueness(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessLogicException("Inventory name cannot be empty");
        }
        
        String trimmedName = name.trim();
        boolean nameExists = inventoryRepository.findByNameIgnoreCase(trimmedName)
                .filter(inventory -> excludeId == null || !inventory.getId().equals(excludeId))
                .isPresent();
        
        if (nameExists) {
            throw new BusinessLogicException("Inventory with name '" + trimmedName + "' already exists");
        }
    }

    private void validateInventoryData(InventoryDTO inventoryDTO) {
        if (inventoryDTO.getName() == null || inventoryDTO.getName().trim().isEmpty()) {
            throw new BusinessLogicException("Inventory name is required");
        }
        
        if (inventoryDTO.getLocation() == null || inventoryDTO.getLocation().trim().isEmpty()) {
            throw new BusinessLogicException("Inventory location is required");
        }

        if (inventoryDTO.getWarehouseCode() != null && !inventoryDTO.getWarehouseCode().trim().isEmpty()) {
            boolean codeExists = inventoryRepository.existsByWarehouseCode(inventoryDTO.getWarehouseCode().trim());
            if (codeExists) {
                throw new BusinessLogicException("Warehouse code '" + inventoryDTO.getWarehouseCode() + "' already exists");
            }
        }

        if (inventoryDTO.getCapacity() != null && inventoryDTO.getCapacity() < 0) {
            throw new BusinessLogicException("Inventory capacity cannot be negative");
        }

        if (inventoryDTO.getCurrentStockCount() != null && inventoryDTO.getCurrentStockCount() < 0) {
            throw new BusinessLogicException("Current stock count cannot be negative");
        }
    }

    private void validateMainWarehouseConstraint(InventoryDTO inventoryDTO, Long excludeId) {
        if (Boolean.TRUE.equals(inventoryDTO.getIsMainWarehouse())) {
            boolean hasOtherMainWarehouse = excludeId != null ? 
                inventoryRepository.existsOtherMainWarehouse(excludeId) :
                inventoryRepository.existsMainWarehouse();
                
            if (hasOtherMainWarehouse) {
                throw new BusinessLogicException("Only one main warehouse is allowed. Please unset the current main warehouse first.");
            }
        }
    }

    private void updateInventoryFields(Inventory existingInventory, InventoryDTO inventoryDTO) {
        existingInventory.setName(inventoryDTO.getName().trim());
        existingInventory.setDescription(inventoryDTO.getDescription());
        existingInventory.setLocation(inventoryDTO.getLocation().trim());
        existingInventory.setAddress(inventoryDTO.getAddress());
        existingInventory.setManagerName(inventoryDTO.getManagerName());
        existingInventory.setManagerPhone(inventoryDTO.getManagerPhone());
        existingInventory.setManagerEmail(inventoryDTO.getManagerEmail());
        existingInventory.setCapacity(inventoryDTO.getCapacity());
        existingInventory.setCurrentStockCount(inventoryDTO.getCurrentStockCount() != null ? inventoryDTO.getCurrentStockCount() : 0);
        existingInventory.setWarehouseCode(inventoryDTO.getWarehouseCode());
        existingInventory.setIsMainWarehouse(inventoryDTO.getIsMainWarehouse() != null ? inventoryDTO.getIsMainWarehouse() : false);
        existingInventory.setOperatingHours(inventoryDTO.getOperatingHours());
        existingInventory.setContactPhone(inventoryDTO.getContactPhone());
        existingInventory.setContactEmail(inventoryDTO.getContactEmail());
        existingInventory.setNotes(inventoryDTO.getNotes());
        
        if (inventoryDTO.getStatus() != null) {
            existingInventory.setStatus(inventoryDTO.getStatus());
        }
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        InventoryDTO dto = InventoryDTO.builder()
                .id(inventory.getId())
                .name(inventory.getName())
                .description(inventory.getDescription())
                .location(inventory.getLocation())
                .address(inventory.getAddress())
                .managerName(inventory.getManagerName())
                .managerPhone(inventory.getManagerPhone())
                .managerEmail(inventory.getManagerEmail())
                .capacity(inventory.getCapacity())
                .currentStockCount(inventory.getCurrentStockCount())
                .status(inventory.getStatus())
                .warehouseCode(inventory.getWarehouseCode())
                .isMainWarehouse(inventory.getIsMainWarehouse())
                .operatingHours(inventory.getOperatingHours())
                .contactPhone(inventory.getContactPhone())
                .contactEmail(inventory.getContactEmail())
                .notes(inventory.getNotes())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .categoryCount(inventory.getCategoryCount())
                .capacityUtilization(inventory.getCapacityUtilization())
                .isNearCapacity(inventory.isNearCapacity(80.0)) // 80% threshold
                .build();
        return dto;
    }

    private Inventory mapToEntity(InventoryDTO inventoryDTO) {
        return Inventory.builder()
                .name(inventoryDTO.getName() != null ? inventoryDTO.getName().trim() : null)
                .description(inventoryDTO.getDescription())
                .location(inventoryDTO.getLocation() != null ? inventoryDTO.getLocation().trim() : null)
                .address(inventoryDTO.getAddress())
                .managerName(inventoryDTO.getManagerName())
                .managerPhone(inventoryDTO.getManagerPhone())
                .managerEmail(inventoryDTO.getManagerEmail())
                .capacity(inventoryDTO.getCapacity())
                .currentStockCount(inventoryDTO.getCurrentStockCount() != null ? inventoryDTO.getCurrentStockCount() : 0)
                .status(inventoryDTO.getStatus() != null ? inventoryDTO.getStatus() : Inventory.InventoryStatus.ACTIVE)
                .warehouseCode(inventoryDTO.getWarehouseCode())
                .isMainWarehouse(inventoryDTO.getIsMainWarehouse() != null ? inventoryDTO.getIsMainWarehouse() : false)
                .operatingHours(inventoryDTO.getOperatingHours())
                .contactPhone(inventoryDTO.getContactPhone())
                .contactEmail(inventoryDTO.getContactEmail())
                .notes(inventoryDTO.getNotes())
                .build();
    }
}
