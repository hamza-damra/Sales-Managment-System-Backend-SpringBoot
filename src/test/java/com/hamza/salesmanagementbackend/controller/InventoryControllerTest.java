package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.InventoryDTO;
import com.hamza.salesmanagementbackend.entity.Inventory;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryDTO testInventoryDTO;
    private List<InventoryDTO> inventoryList;

    @BeforeEach
    void setUp() {
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
                .capacityUtilization(50.0)
                .isNearCapacity(false)
                .build();

        InventoryDTO secondInventory = InventoryDTO.builder()
                .id(2L)
                .name("Second Warehouse")
                .description("Second description")
                .location("Second Location")
                .capacity(200)
                .currentStockCount(160)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("SW001")
                .isMainWarehouse(true)
                .capacityUtilization(80.0)
                .isNearCapacity(true)
                .build();

        inventoryList = Arrays.asList(testInventoryDTO, secondInventory);
    }

    @Test
    void getAllInventories_ShouldReturnPagedInventories() throws Exception {
        // Given
        Page<InventoryDTO> inventoryPage = new PageImpl<>(inventoryList, PageRequest.of(0, 10), 2);
        when(inventoryService.getAllInventories(any())).thenReturn(inventoryPage);

        // When & Then
        mockMvc.perform(get("/api/inventories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Test Warehouse")))
                .andExpect(jsonPath("$.content[1].name", is("Second Warehouse")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getAllActiveInventories_ShouldReturnActiveInventories() throws Exception {
        // Given
        when(inventoryService.getAllActiveInventories()).thenReturn(inventoryList);

        // When & Then
        mockMvc.perform(get("/api/inventories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[1].status", is("ACTIVE")));
    }

    @Test
    void getMainWarehouses_ShouldReturnMainWarehouses() throws Exception {
        // Given
        List<InventoryDTO> mainWarehouses = Arrays.asList(inventoryList.get(1)); // Second warehouse is main
        when(inventoryService.getMainWarehouses()).thenReturn(mainWarehouses);

        // When & Then
        mockMvc.perform(get("/api/inventories/main-warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isMainWarehouse", is(true)))
                .andExpect(jsonPath("$[0].name", is("Second Warehouse")));
    }

    @Test
    void searchInventories_ShouldReturnMatchingInventories() throws Exception {
        // Given
        Page<InventoryDTO> searchResults = new PageImpl<>(Arrays.asList(testInventoryDTO));
        when(inventoryService.searchInventories(eq("test"), any())).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/inventories/search")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test Warehouse")));
    }

    @Test
    void searchInventories_ShouldReturnBadRequest_WhenQueryIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/search")
                        .param("query", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchInventories_ShouldReturnBadRequest_WhenQueryIsNull() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventoryById_ShouldReturnInventory_WhenExists() throws Exception {
        // Given
        when(inventoryService.getInventoryById(1L)).thenReturn(testInventoryDTO);

        // When & Then
        mockMvc.perform(get("/api/inventories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Warehouse")))
                .andExpect(jsonPath("$.location", is("Test Location")))
                .andExpect(jsonPath("$.capacityUtilization", is(50.0)))
                .andExpect(jsonPath("$.isNearCapacity", is(false)));
    }

    @Test
    void getInventoryById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(inventoryService.getInventoryById(1L)).thenThrow(new ResourceNotFoundException("Inventory not found"));

        // When & Then
        mockMvc.perform(get("/api/inventories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInventoryById_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/inventories/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_ShouldCreateSuccessfully_WhenValidData() throws Exception {
        // Given
        InventoryDTO newInventoryDTO = InventoryDTO.builder()
                .name("New Warehouse")
                .description("New description")
                .location("New Location")
                .capacity(150)
                .currentStockCount(0)
                .status(Inventory.InventoryStatus.ACTIVE)
                .isMainWarehouse(false)
                .build();

        InventoryDTO createdInventory = InventoryDTO.builder()
                .id(3L)
                .name("New Warehouse")
                .description("New description")
                .location("New Location")
                .capacity(150)
                .currentStockCount(0)
                .status(Inventory.InventoryStatus.ACTIVE)
                .isMainWarehouse(false)
                .build();

        when(inventoryService.createInventory(any(InventoryDTO.class))).thenReturn(createdInventory);

        // When & Then
        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInventoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Warehouse")))
                .andExpect(jsonPath("$.location", is("New Location")));

        verify(inventoryService).createInventory(any(InventoryDTO.class));
    }

    @Test
    void createInventory_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given
        InventoryDTO invalidInventory = InventoryDTO.builder()
                .name("") // Invalid: empty name
                .location("") // Invalid: empty location
                .build();

        // When & Then
        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInventory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_ShouldUpdateSuccessfully_WhenValidData() throws Exception {
        // Given
        InventoryDTO updatedInventory = InventoryDTO.builder()
                .id(1L)
                .name("Updated Warehouse")
                .description("Updated description")
                .location("Updated Location")
                .capacity(200)
                .currentStockCount(100)
                .status(Inventory.InventoryStatus.ACTIVE)
                .isMainWarehouse(false)
                .build();

        when(inventoryService.updateInventory(eq(1L), any(InventoryDTO.class))).thenReturn(updatedInventory);

        // When & Then
        mockMvc.perform(put("/api/inventories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Warehouse")))
                .andExpect(jsonPath("$.description", is("Updated description")));

        verify(inventoryService).updateInventory(eq(1L), any(InventoryDTO.class));
    }

    @Test
    void updateInventory_ShouldReturnNotFound_WhenInventoryNotExists() throws Exception {
        // Given
        when(inventoryService.updateInventory(eq(1L), any(InventoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Inventory not found"));

        // When & Then
        mockMvc.perform(put("/api/inventories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInventory_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/inventories/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteInventory_ShouldDeleteSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/inventories/1"))
                .andExpect(status().isNoContent());

        verify(inventoryService).deleteInventory(1L);
    }

    @Test
    void deleteInventory_ShouldReturnNotFound_WhenInventoryNotExists() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Inventory not found")).when(inventoryService).deleteInventory(1L);

        // When & Then
        mockMvc.perform(delete("/api/inventories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteInventory_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/inventories/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventoriesByStatus_ShouldReturnInventoriesWithSpecificStatus() throws Exception {
        // Given
        when(inventoryService.getInventoriesByStatus(Inventory.InventoryStatus.ACTIVE)).thenReturn(inventoryList);

        // When & Then
        mockMvc.perform(get("/api/inventories/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[1].status", is("ACTIVE")));
    }

    @Test
    void getInventoriesByStatus_ShouldReturnBadRequest_WhenStatusIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmptyInventories_ShouldReturnEmptyInventories() throws Exception {
        // Given
        when(inventoryService.getEmptyInventories()).thenReturn(Arrays.asList(testInventoryDTO));

        // When & Then
        mockMvc.perform(get("/api/inventories/empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Warehouse")));
    }

    @Test
    void getInventoriesNearCapacity_ShouldReturnInventoriesAboveThreshold() throws Exception {
        // Given
        List<InventoryDTO> nearCapacityInventories = Arrays.asList(inventoryList.get(1)); // Second warehouse at 80%
        when(inventoryService.getInventoriesNearCapacity(80.0)).thenReturn(nearCapacityInventories);

        // When & Then
        mockMvc.perform(get("/api/inventories/near-capacity")
                        .param("threshold", "80.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isNearCapacity", is(true)));
    }

    @Test
    void getInventoriesNearCapacity_ShouldUseDefaultThreshold_WhenNotProvided() throws Exception {
        // Given
        when(inventoryService.getInventoriesNearCapacity(80.0)).thenReturn(Arrays.asList(inventoryList.get(1)));

        // When & Then
        mockMvc.perform(get("/api/inventories/near-capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getInventoriesNearCapacity_ShouldReturnBadRequest_WhenThresholdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/near-capacity")
                        .param("threshold", "-10"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/inventories/near-capacity")
                        .param("threshold", "150"))
                .andExpect(status().isBadRequest());
    }

    // Additional Error Handling Tests

    @Test
    void createInventory_ShouldHandleBusinessLogicException() throws Exception {
        // Given
        when(inventoryService.createInventory(any(InventoryDTO.class)))
                .thenThrow(new BusinessLogicException("Inventory name already exists"));

        // When & Then
        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteInventory_ShouldHandleDataIntegrityException() throws Exception {
        // Given
        doThrow(new DataIntegrityException("Inventory", 1L, "Categories", "Cannot delete inventory with categories"))
                .when(inventoryService).deleteInventory(1L);

        // When & Then
        mockMvc.perform(delete(ApplicationConstants.API_INVENTORIES + "/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void getInventoryByWarehouseCode_ShouldReturnInventory_WhenExists() throws Exception {
        // Given
        when(inventoryService.getInventoryByWarehouseCode("TW001")).thenReturn(testInventoryDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_INVENTORIES + "/warehouse-code/TW001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehouseCode", is("TW001")))
                .andExpect(jsonPath("$.name", is("Test Warehouse")));
    }

    @Test
    void getInventoryByWarehouseCode_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(inventoryService.getInventoryByWarehouseCode("INVALID"))
                .thenThrow(new ResourceNotFoundException("Inventory not found"));

        // When & Then
        mockMvc.perform(get("/api/inventories/warehouse-code/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInventoryByWarehouseCode_ShouldReturnBadRequest_WhenCodeIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/inventories/warehouse-code/ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllInventories_ShouldHandleInvalidSortParameters() throws Exception {
        // Given
        Page<InventoryDTO> inventoryPage = new PageImpl<>(inventoryList);
        when(inventoryService.getAllInventories(any())).thenReturn(inventoryPage);

        // When & Then - Should handle invalid sort direction gracefully
        mockMvc.perform(get("/api/inventories")
                        .param("sortBy", "name")
                        .param("sortDir", "invalid"))
                .andExpect(status().isOk()); // Should default to asc
    }

    @Test
    void searchInventories_ShouldTrimQuery() throws Exception {
        // Given
        Page<InventoryDTO> searchResults = new PageImpl<>(Arrays.asList(testInventoryDTO));
        when(inventoryService.searchInventories(eq("test"), any())).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/inventories/search")
                        .param("query", "  test  ") // Query with spaces
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void createInventory_ShouldHandleNullValues() throws Exception {
        // Given
        InventoryDTO inventoryWithNulls = InventoryDTO.builder()
                .name("Valid Name")
                .location("Valid Location")
                .description(null)
                .capacity(null)
                .currentStockCount(null)
                .isMainWarehouse(null)
                .build();

        InventoryDTO createdInventory = InventoryDTO.builder()
                .id(1L)
                .name("Valid Name")
                .location("Valid Location")
                .description(null)
                .capacity(null)
                .currentStockCount(0)
                .isMainWarehouse(false)
                .build();

        when(inventoryService.createInventory(any(InventoryDTO.class))).thenReturn(createdInventory);

        // When & Then
        mockMvc.perform(post("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryWithNulls)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Valid Name")))
                .andExpect(jsonPath("$.currentStockCount", is(0)))
                .andExpect(jsonPath("$.isMainWarehouse", is(false)));
    }

    @Test
    void updateInventory_ShouldHandleBusinessLogicException() throws Exception {
        // Given
        when(inventoryService.updateInventory(eq(1L), any(InventoryDTO.class)))
                .thenThrow(new BusinessLogicException("Only one main warehouse is allowed"));

        // When & Then
        mockMvc.perform(put("/api/inventories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInventoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllInventories_ShouldHandleEmptyResults() throws Exception {
        // Given
        Page<InventoryDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(inventoryService.getAllInventories(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void searchInventories_ShouldHandleEmptyResults() throws Exception {
        // Given
        Page<InventoryDTO> emptyResults = new PageImpl<>(Arrays.asList());
        when(inventoryService.searchInventories(eq("nonexistent"), any())).thenReturn(emptyResults);

        // When & Then
        mockMvc.perform(get("/api/inventories/search")
                        .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
