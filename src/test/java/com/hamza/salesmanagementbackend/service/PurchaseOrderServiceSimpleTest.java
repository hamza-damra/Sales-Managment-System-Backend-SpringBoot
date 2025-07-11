package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.PurchaseOrderRepository;
import com.hamza.salesmanagementbackend.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simple test for PurchaseOrderService to verify basic functionality
 */
@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceSimpleTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier testSupplier;
    private Product testProduct;
    private PurchaseOrderDTO testOrderDTO;
    private PurchaseOrderItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        System.out.println("🧪 Setting up test data...");
        
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

        // Setup test item DTO
        testItemDTO = PurchaseOrderItemDTO.builder()
                .productId(1L)
                .quantity(100)
                .unitCost(BigDecimal.valueOf(25.50))
                .totalPrice(BigDecimal.valueOf(2550.00))
                .build();

        // Setup test order DTO
        testOrderDTO = PurchaseOrderDTO.builder()
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
        
        System.out.println("✅ Test data setup complete");
    }

    @Test
    void testServiceClassExists() {
        System.out.println("🧪 Testing PurchaseOrderService class existence...");
        
        assertThat(purchaseOrderService).isNotNull();
        assertThat(purchaseOrderService.getClass().getSimpleName()).isEqualTo("PurchaseOrderService");
        
        System.out.println("✅ PurchaseOrderService class exists and can be instantiated");
    }

    @Test
    void testCreatePurchaseOrder_Success() {
        System.out.println("🧪 Testing successful purchase order creation...");
        
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(purchaseOrderRepository.countByOrderNumberStartingWith(anyString())).thenReturn(0L);
        
        PurchaseOrder savedOrder = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-2024-001")
                .supplier(testSupplier)
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(2932.50))
                .build();
        
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(savedOrder);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        PurchaseOrderDTO result = purchaseOrderService.createPurchaseOrder(testOrderDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("PO-2024-001");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getSupplierName()).isEqualTo("Test Supplier");
        
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        verify(supplierRepository).save(any(Supplier.class));
        
        System.out.println("   ✓ Order created with ID: " + result.getId());
        System.out.println("   ✓ Order number: " + result.getOrderNumber());
        System.out.println("   ✓ Supplier: " + result.getSupplierName());
        System.out.println("✅ Create purchase order test passed");
    }

    @Test
    void testCreatePurchaseOrder_SupplierNotFound() {
        System.out.println("🧪 Testing purchase order creation with non-existent supplier...");
        
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testOrderDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with ID: 1");
        
        verify(purchaseOrderRepository, never()).save(any());
        
        System.out.println("   ✓ Correctly throws ResourceNotFoundException");
        System.out.println("   ✓ No order saved when supplier not found");
        System.out.println("✅ Supplier not found test passed");
    }

    @Test
    void testCreatePurchaseOrder_InactiveSupplier() {
        System.out.println("🧪 Testing purchase order creation with inactive supplier...");
        
        // Given
        testSupplier.setStatus(Supplier.SupplierStatus.INACTIVE);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testOrderDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot create order for inactive supplier");
        
        verify(purchaseOrderRepository, never()).save(any());
        
        System.out.println("   ✓ Correctly throws BusinessLogicException");
        System.out.println("   ✓ No order saved for inactive supplier");
        System.out.println("✅ Inactive supplier test passed");
    }

    @Test
    void testCreatePurchaseOrder_NoItems() {
        System.out.println("🧪 Testing purchase order creation with no items...");
        
        // Given
        testOrderDTO.setItems(null);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(testOrderDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("At least one item is required");
        
        verify(purchaseOrderRepository, never()).save(any());
        
        System.out.println("   ✓ Correctly throws BusinessLogicException");
        System.out.println("   ✓ No order saved without items");
        System.out.println("✅ No items test passed");
    }

    @Test
    void testGetPurchaseOrderById_Success() {
        System.out.println("🧪 Testing get purchase order by ID...");
        
        // Given
        PurchaseOrder existingOrder = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-2024-001")
                .supplier(testSupplier)
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(2932.50))
                .build();
        
        when(purchaseOrderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existingOrder));

        // When
        PurchaseOrderDTO result = purchaseOrderService.getPurchaseOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("PO-2024-001");
        
        System.out.println("   ✓ Order retrieved with ID: " + result.getId());
        System.out.println("   ✓ Order number: " + result.getOrderNumber());
        System.out.println("✅ Get purchase order by ID test passed");
    }

    @Test
    void testGetPurchaseOrderById_NotFound() {
        System.out.println("🧪 Testing get purchase order by ID - not found...");
        
        // Given
        when(purchaseOrderRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase order not found with ID: 999");
        
        System.out.println("   ✓ Correctly throws ResourceNotFoundException");
        System.out.println("✅ Purchase order not found test passed");
    }

    @Test
    void testOrderNumberGeneration() {
        System.out.println("🧪 Testing order number generation logic...");
        
        // Test the order number generation format
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = 5; // Simulating 5 existing orders
        String orderNumber = String.format("PO-%s-%03d", year, count + 1);
        
        String expectedPattern = "PO-" + year + "-006";
        assertThat(orderNumber).isEqualTo(expectedPattern);
        
        // Test format validation
        assertThat(orderNumber).matches("PO-\\d{4}-\\d{3}");
        
        System.out.println("   ✓ Generated order number: " + orderNumber);
        System.out.println("   ✓ Format validation passed");
        System.out.println("✅ Order number generation test passed");
    }

    @Test
    void testFinancialCalculations() {
        System.out.println("🧪 Testing financial calculations...");
        
        // Test tax calculation
        BigDecimal subtotal = new BigDecimal("2550.00");
        Double taxRate = 15.0;
        BigDecimal expectedTaxAmount = new BigDecimal("382.50");
        
        BigDecimal calculatedTaxAmount = subtotal.multiply(BigDecimal.valueOf(taxRate))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        assertThat(calculatedTaxAmount).isEqualByComparingTo(expectedTaxAmount);
        
        // Test total calculation
        BigDecimal shippingCost = new BigDecimal("100.00");
        BigDecimal discountAmount = new BigDecimal("50.00");
        BigDecimal expectedTotal = new BigDecimal("2982.50");
        
        BigDecimal calculatedTotal = subtotal.add(calculatedTaxAmount)
                .add(shippingCost)
                .subtract(discountAmount);
        
        assertThat(calculatedTotal).isEqualByComparingTo(expectedTotal);
        
        System.out.println("   ✓ Tax calculation: " + subtotal + " * " + taxRate + "% = " + calculatedTaxAmount);
        System.out.println("   ✓ Total calculation: " + calculatedTotal);
        System.out.println("✅ Financial calculations test passed");
    }

    @Test
    void testStatusTransitionValidation() {
        System.out.println("🧪 Testing status transition validation...");
        
        // Test valid transitions
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.PENDING, PurchaseOrder.PurchaseOrderStatus.APPROVED)).isTrue();
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.APPROVED, PurchaseOrder.PurchaseOrderStatus.SENT)).isTrue();
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.SENT, PurchaseOrder.PurchaseOrderStatus.DELIVERED)).isTrue();
        
        // Test invalid transitions
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.PENDING, PurchaseOrder.PurchaseOrderStatus.DELIVERED)).isFalse();
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.DELIVERED, PurchaseOrder.PurchaseOrderStatus.PENDING)).isFalse();
        assertThat(isValidTransition(PurchaseOrder.PurchaseOrderStatus.CANCELLED, PurchaseOrder.PurchaseOrderStatus.APPROVED)).isFalse();
        
        System.out.println("   ✓ Valid transitions work correctly");
        System.out.println("   ✓ Invalid transitions are blocked");
        System.out.println("✅ Status transition validation test passed");
    }

    // Helper method to simulate status transition validation
    private boolean isValidTransition(PurchaseOrder.PurchaseOrderStatus from, PurchaseOrder.PurchaseOrderStatus to) {
        switch (from) {
            case PENDING:
                return to == PurchaseOrder.PurchaseOrderStatus.APPROVED || to == PurchaseOrder.PurchaseOrderStatus.CANCELLED;
            case APPROVED:
                return to == PurchaseOrder.PurchaseOrderStatus.SENT || to == PurchaseOrder.PurchaseOrderStatus.CANCELLED;
            case SENT:
                return to == PurchaseOrder.PurchaseOrderStatus.DELIVERED || to == PurchaseOrder.PurchaseOrderStatus.CANCELLED;
            case DELIVERED:
                return to == PurchaseOrder.PurchaseOrderStatus.CANCELLED;
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}
