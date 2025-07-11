package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import com.hamza.salesmanagementbackend.entity.PurchaseOrderItem;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.PurchaseOrderItemRepository;
import com.hamza.salesmanagementbackend.repository.PurchaseOrderRepository;
import com.hamza.salesmanagementbackend.repository.SupplierRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier testSupplier;
    private Product testProduct;
    private PurchaseOrder testPurchaseOrder;
    private PurchaseOrderDTO testPurchaseOrderDTO;
    private PurchaseOrderItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        // Setup test supplier
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .email("supplier@test.com")
                .status(Supplier.SupplierStatus.ACTIVE)
                .paymentTerms("NET_30")
                .deliveryTerms("FOB_DESTINATION")
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(25.50))
                .build();

        // Setup test purchase order item DTO
        testItemDTO = PurchaseOrderItemDTO.builder()
                .productId(1L)
                .quantity(100)
                .unitCost(BigDecimal.valueOf(25.50))
                .totalPrice(BigDecimal.valueOf(2550.00))
                .build();

        // Setup test purchase order DTO
        testPurchaseOrderDTO = PurchaseOrderDTO.builder()
                .supplierId(1L)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(10))
                .shippingAddress("123 Test Street")
                .priority(PurchaseOrder.OrderPriority.NORMAL)
                .taxRate(15.0)
                .items(Arrays.asList(testItemDTO))
                .subtotal(BigDecimal.valueOf(2550.00))
                .taxAmount(BigDecimal.valueOf(382.50))
                .totalAmount(BigDecimal.valueOf(2932.50))
                .notes("Test order")
                .build();

        // Setup test purchase order entity
        testPurchaseOrder = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-2024-001")
                .supplier(testSupplier)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(10))
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .priority(PurchaseOrder.OrderPriority.NORMAL)
                .shippingAddress("123 Test Street")
                .taxRate(15.0)
                .subtotal(BigDecimal.valueOf(2550.00))
                .taxAmount(BigDecimal.valueOf(382.50))
                .totalAmount(BigDecimal.valueOf(2932.50))
                .notes("Test order")
                .build();
    }

    @Test
    void createPurchaseOrder_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(purchaseOrderRepository.countByOrderNumberStartingWith(anyString())).thenReturn(0L);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        PurchaseOrderDTO result = purchaseOrderService.createPurchaseOrder(testPurchaseOrderDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getSupplierName()).isEqualTo("Test Supplier");
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(2932.50));
        
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void createPurchaseOrder_ShouldThrowException_WhenSupplierNotFound() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with ID: 1");
    }

    @Test
    void createPurchaseOrder_ShouldThrowException_WhenSupplierInactive() {
        // Given
        testSupplier.setStatus(Supplier.SupplierStatus.INACTIVE);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot create order for inactive supplier");
    }

    @Test
    void createPurchaseOrder_ShouldThrowException_WhenNoItems() {
        // Given
        testPurchaseOrderDTO.setItems(null);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("At least one item is required");
    }

    @Test
    void getPurchaseOrderById_ShouldReturnOrder_WhenExists() {
        // Given
        when(purchaseOrderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testPurchaseOrder));

        // When
        PurchaseOrderDTO result = purchaseOrderService.getPurchaseOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("PO-2024-001");
    }

    @Test
    void getPurchaseOrderById_ShouldThrowException_WhenNotFound() {
        // Given
        when(purchaseOrderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase order not found with ID: 1");
    }

    @Test
    void getAllPurchaseOrders_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<PurchaseOrder> mockPage = new PageImpl<>(Arrays.asList(testPurchaseOrder));
        when(purchaseOrderRepository.findAll(pageable)).thenReturn(mockPage);

        // When
        Page<PurchaseOrderDTO> result = purchaseOrderService.getAllPurchaseOrders(
                pageable, null, null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void updatePurchaseOrderStatus_ShouldUpdateSuccessfully_WhenValidTransition() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

        // When
        PurchaseOrderDTO result = purchaseOrderService.updatePurchaseOrderStatus(
                1L, PurchaseOrder.PurchaseOrderStatus.APPROVED, "Approved by manager", null);

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    void approvePurchaseOrder_ShouldApproveSuccessfully_WhenPending() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

        // When
        PurchaseOrderDTO result = purchaseOrderService.approvePurchaseOrder(
                1L, "Approved for procurement", "manager@test.com");

        // Then
        assertThat(result).isNotNull();
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    void deletePurchaseOrder_ShouldDeleteSuccessfully_WhenPending() {
        // Given
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPurchaseOrder));

        // When
        purchaseOrderService.deletePurchaseOrder(1L);

        // Then
        verify(purchaseOrderRepository).delete(testPurchaseOrder);
    }

    @Test
    void deletePurchaseOrder_ShouldThrowException_WhenNotPending() {
        // Given
        testPurchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.APPROVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPurchaseOrder));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.deletePurchaseOrder(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot delete order with status: APPROVED");
    }
}
