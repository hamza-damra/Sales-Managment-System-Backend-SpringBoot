package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.InventoryDTO;
import com.hamza.salesmanagementbackend.entity.Inventory;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private InventoryDTO testInventoryDTO;
    private Inventory mainWarehouse;

    @BeforeEach
    void setUp() {
        testInventory = Inventory.builder()
                .id(1L)
                .name("Test Warehouse")
                .description("Test description")
                .location("Test Location")
                .address("123 Test St")
                .managerName("Test Manager")
                .managerEmail("test@example.com")
                .capacity(100)
                .currentStockCount(50)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("TW001")
                .isMainWarehouse(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testInventoryDTO = InventoryDTO.builder()
                .id(1L)
                .name("Test Warehouse")
                .description("Test description")
                .location("Test Location")
                .address("123 Test St")
                .managerName("Test Manager")
                .managerEmail("test@example.com")
                .capacity(100)
                .currentStockCount(50)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("TW001")
                .isMainWarehouse(false)
                .build();

        mainWarehouse = Inventory.builder()
                .id(2L)
                .name("Main Warehouse")
                .description("Main warehouse")
                .location("Main Location")
                .capacity(500)
                .currentStockCount(250)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("MW001")
                .isMainWarehouse(true)
                .build();
    }

    @Test
    void createInventory_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.existsByWarehouseCode(anyString())).thenReturn(false);
        when(inventoryRepository.existsMainWarehouse()).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryDTO result = inventoryService.createInventory(testInventoryDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Warehouse");
        assertThat(result.getLocation()).isEqualTo("Test Location");
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_ShouldThrowException_WhenNameAlreadyExists() {
        // Given
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(testInventory));

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(testInventoryDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createInventory_ShouldThrowException_WhenNameIsEmpty() {
        // Given
        testInventoryDTO.setName("");

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(testInventoryDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("name cannot be empty");
    }

    @Test
    void createInventory_ShouldThrowException_WhenLocationIsEmpty() {
        // Given
        testInventoryDTO.setLocation("");
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(testInventoryDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("location is required");
    }

    @Test
    void createInventory_ShouldThrowException_WhenWarehouseCodeAlreadyExists() {
        // Given
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.existsByWarehouseCode(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(testInventoryDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createInventory_ShouldThrowException_WhenTryingToCreateSecondMainWarehouse() {
        // Given
        testInventoryDTO.setIsMainWarehouse(true);
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.existsByWarehouseCode(anyString())).thenReturn(false);
        when(inventoryRepository.existsMainWarehouse()).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(testInventoryDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Only one main warehouse is allowed");
    }

    @Test
    void getAllInventories_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> inventoryPage = new PageImpl<>(Arrays.asList(testInventory));
        when(inventoryRepository.findAll(pageable)).thenReturn(inventoryPage);

        // When
        Page<InventoryDTO> result = inventoryService.getAllInventories(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Warehouse");
    }

    @Test
    void getAllActiveInventories_ShouldReturnOnlyActiveInventories() {
        // Given
        when(inventoryRepository.findByStatus(Inventory.InventoryStatus.ACTIVE))
                .thenReturn(Arrays.asList(testInventory));

        // When
        List<InventoryDTO> result = inventoryService.getAllActiveInventories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Inventory.InventoryStatus.ACTIVE);
    }

    @Test
    void getInventoryById_ShouldReturnInventory_WhenExists() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        // When
        InventoryDTO result = inventoryService.getInventoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Warehouse");
    }

    @Test
    void getInventoryById_ShouldThrowException_WhenNotExists() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.getInventoryById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 1");
    }

    @Test
    void updateInventory_ShouldUpdateSuccessfully_WhenValidData() {
        // Given
        InventoryDTO updateDTO = InventoryDTO.builder()
                .name("Updated Warehouse")
                .description("Updated description")
                .location("Updated Location")
                .capacity(200)
                .currentStockCount(100)
                .status(Inventory.InventoryStatus.ACTIVE)
                .isMainWarehouse(false)
                .build();

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(inventoryRepository.existsByWarehouseCode(anyString())).thenReturn(false);
        when(inventoryRepository.existsOtherMainWarehouse(anyLong())).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryDTO result = inventoryService.updateInventory(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_ShouldThrowException_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateInventory(1L, testInventoryDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 1");
    }

    @Test
    void deleteInventory_ShouldDeleteSuccessfully_WhenNoCategories() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.countCategoriesByInventoryId(1L)).thenReturn(0L);

        // When
        inventoryService.deleteInventory(1L);

        // Then
        verify(inventoryRepository).deleteById(1L);
    }

    @Test
    void deleteInventory_ShouldThrowException_WhenHasCategories() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.countCategoriesByInventoryId(1L)).thenReturn(3L);

        // When & Then
        assertThatThrownBy(() -> inventoryService.deleteInventory(1L))
                .isInstanceOf(DataIntegrityException.class);
    }

    @Test
    void deleteInventory_ShouldThrowException_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.deleteInventory(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 1");
    }

    @Test
    void searchInventories_ShouldReturnMatchingResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> inventoryPage = new PageImpl<>(Arrays.asList(testInventory));
        when(inventoryRepository.searchInventories("test", pageable)).thenReturn(inventoryPage);

        // When
        Page<InventoryDTO> result = inventoryService.searchInventories("test", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getInventoriesByStatus_ShouldReturnInventoriesWithSpecificStatus() {
        // Given
        when(inventoryRepository.findByStatus(Inventory.InventoryStatus.INACTIVE))
                .thenReturn(Arrays.asList(testInventory));

        // When
        List<InventoryDTO> result = inventoryService.getInventoriesByStatus(Inventory.InventoryStatus.INACTIVE);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getMainWarehouses_ShouldReturnMainWarehouses() {
        // Given
        when(inventoryRepository.findMainWarehouses()).thenReturn(Arrays.asList(mainWarehouse));

        // When
        List<InventoryDTO> result = inventoryService.getMainWarehouses();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsMainWarehouse()).isTrue();
    }

    @Test
    void getEmptyInventories_ShouldReturnInventoriesWithoutCategories() {
        // Given
        when(inventoryRepository.findEmptyInventories()).thenReturn(Arrays.asList(testInventory));

        // When
        List<InventoryDTO> result = inventoryService.getEmptyInventories();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getInventoriesNearCapacity_ShouldReturnInventoriesAboveThreshold() {
        // Given
        when(inventoryRepository.findInventoriesNearCapacity(80.0)).thenReturn(Arrays.asList(testInventory));

        // When
        List<InventoryDTO> result = inventoryService.getInventoriesNearCapacity(80.0);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void updateInventoryStatus_ShouldUpdateStatusSuccessfully() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryDTO result = inventoryService.updateInventoryStatus(1L, Inventory.InventoryStatus.MAINTENANCE);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventoryStatus_ShouldThrowException_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateInventoryStatus(1L, Inventory.InventoryStatus.MAINTENANCE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found with id: 1");
    }
}
