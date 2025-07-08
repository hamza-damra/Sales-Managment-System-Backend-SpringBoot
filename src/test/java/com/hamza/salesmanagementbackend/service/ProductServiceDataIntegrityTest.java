package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceDataIntegrityTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void deleteProduct_WithAssociatedSaleItems_ThrowsDataIntegrityException() {
        // Given
        Long productId = 1L;
        Long saleItemCount = 10L;
        Long returnItemCount = 0L;
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.countSaleItemsByProductId(productId)).thenReturn(saleItemCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> productService.deleteProduct(productId));

        assertEquals("Product", exception.getResourceType());
        assertEquals(productId, exception.getResourceId());
        assertEquals("Sale Items", exception.getDependentResource());
        assertEquals("Cannot delete product because it appears in 10 sale records", exception.getUserMessage());
        assertEquals("PRODUCT_HAS_SALE_ITEMS", exception.getErrorCode());

        verify(productRepository).existsById(productId);
        verify(productRepository).countSaleItemsByProductId(productId);
        verify(productRepository, never()).countReturnItemsByProductId(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void deleteProduct_WithAssociatedReturnItems_ThrowsDataIntegrityException() {
        // Given
        Long productId = 2L;
        Long saleItemCount = 0L;
        Long returnItemCount = 5L;
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.countSaleItemsByProductId(productId)).thenReturn(saleItemCount);
        when(productRepository.countReturnItemsByProductId(productId)).thenReturn(returnItemCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> productService.deleteProduct(productId));

        assertEquals("Product", exception.getResourceType());
        assertEquals(productId, exception.getResourceId());
        assertEquals("Return Items", exception.getDependentResource());
        assertEquals("Cannot delete product because it appears in 5 return records", exception.getUserMessage());
        assertEquals("PRODUCT_HAS_RETURN_ITEMS", exception.getErrorCode());

        verify(productRepository).existsById(productId);
        verify(productRepository).countSaleItemsByProductId(productId);
        verify(productRepository).countReturnItemsByProductId(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void deleteProduct_WithoutDependencies_Success() {
        // Given
        Long productId = 3L;
        Long saleItemCount = 0L;
        Long returnItemCount = 0L;
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.countSaleItemsByProductId(productId)).thenReturn(saleItemCount);
        when(productRepository.countReturnItemsByProductId(productId)).thenReturn(returnItemCount);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).existsById(productId);
        verify(productRepository).countSaleItemsByProductId(productId);
        verify(productRepository).countReturnItemsByProductId(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void deleteProduct_ProductNotFound_ThrowsResourceNotFoundException() {
        // Given
        Long productId = 999L;
        
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId));

        assertEquals("Product not found with id: 999", exception.getMessage());
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).countSaleItemsByProductId(productId);
        verify(productRepository, never()).countReturnItemsByProductId(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    void deleteProduct_WithSingleSaleItem_CorrectMessage() {
        // Given
        Long productId = 4L;
        Long saleItemCount = 1L;
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.countSaleItemsByProductId(productId)).thenReturn(saleItemCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> productService.deleteProduct(productId));

        assertEquals("Cannot delete product because it appears in 1 sale record", exception.getUserMessage());
        assertTrue(exception.getSuggestion().contains("marking it as inactive"));
    }

    @Test
    void deleteProduct_WithSingleReturnItem_CorrectMessage() {
        // Given
        Long productId = 5L;
        Long saleItemCount = 0L;
        Long returnItemCount = 1L;
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.countSaleItemsByProductId(productId)).thenReturn(saleItemCount);
        when(productRepository.countReturnItemsByProductId(productId)).thenReturn(returnItemCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> productService.deleteProduct(productId));

        assertEquals("Cannot delete product because it appears in 1 return record", exception.getUserMessage());
    }
}
