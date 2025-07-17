package com.hamza.salesmanagementbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Response DTO for the recent products endpoint
 * Contains both paginated product data and inventory summary statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentProductsResponseDTO {

    /**
     * Paginated list of recent products
     */
    private Page<ProductDTO> products;

    /**
     * Inventory summary statistics
     */
    private InventorySummaryDTO inventorySummary;
}
