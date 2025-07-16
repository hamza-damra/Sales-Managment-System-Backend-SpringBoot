package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.SaleService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @GetMapping
    public ResponseEntity<Page<SaleDTO>> getAllSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createSaleSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<SaleDTO> sales;
        if (status != null) {
            sales = saleService.getSalesByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            sales = saleService.getSalesByDateRange(startDate, endDate, pageable);
        } else {
            sales = saleService.getAllSales(pageable);
        }

        return ResponseEntity.ok(sales);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getSaleById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SaleDTO sale = saleService.getSaleById(id);
            return ResponseEntity.ok(sale);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<SaleDTO>> getSalesByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (customerId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Validate pagination parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, Sort.by("saleDate").descending());
        Page<SaleDTO> sales = saleService.getSalesByCustomer(customerId, pageable);
        return ResponseEntity.ok(sales);
    }

    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@Valid @RequestBody SaleDTO saleDTO,
                                             @RequestParam(required = false) String couponCode) {
        SaleDTO createdSale;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            createdSale = saleService.createSaleWithPromotion(saleDTO, couponCode);
        } else {
            createdSale = saleService.createSale(saleDTO);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSale);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleDTO> updateSale(@PathVariable Long id,
                                             @Valid @RequestBody SaleDTO saleDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SaleDTO updatedSale = saleService.updateSale(id, saleDTO);
        return ResponseEntity.ok(updatedSale);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SaleDTO> completeSale(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SaleDTO completedSale = saleService.completeSale(id);
        return ResponseEntity.ok(completedSale);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SaleDTO> cancelSale(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SaleDTO cancelledSale = saleService.cancelSale(id);
        return ResponseEntity.ok(cancelledSale);
    }

    @PostMapping("/{id}/apply-promotion")
    public ResponseEntity<SaleDTO> applyPromotionToSale(@PathVariable Long id,
                                                       @RequestParam String couponCode) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (couponCode == null || couponCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SaleDTO updatedSale = saleService.applyPromotionToExistingSale(id, couponCode);
            return ResponseEntity.ok(updatedSale);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessLogicException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/remove-promotion")
    public ResponseEntity<SaleDTO> removePromotionFromSale(@PathVariable Long id,
                                                          @RequestParam Long promotionId) {
        if (id <= 0 || promotionId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SaleDTO updatedSale = saleService.removePromotionFromSale(id, promotionId);
            return ResponseEntity.ok(updatedSale);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (BusinessLogicException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/eligible-promotions")
    public ResponseEntity<List<PromotionDTO>> getEligiblePromotionsForSale(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<PromotionDTO> eligiblePromotions = saleService.getEligiblePromotionsForSale(id);
            return ResponseEntity.ok(eligiblePromotions);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
