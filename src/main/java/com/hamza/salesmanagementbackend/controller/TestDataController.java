package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to provide test data information for API testing
 * This controller helps developers understand what data is available for testing
 */
@RestController
@RequestMapping(ApplicationConstants.API_TEST_DATA)
@CrossOrigin(origins = "*")
public class TestDataController {

    @Autowired
    private TestDataService testDataService;

    /**
     * Get all available test data for returns testing
     */
    @GetMapping(ApplicationConstants.INFO_ENDPOINT)
    public ResponseEntity<TestDataService.TestDataInfo> getTestDataInfo() {
        TestDataService.TestDataInfo info = testDataService.getTestDataInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Generate a valid return request payload for testing
     */
    @GetMapping("/valid-return-request")
    public ResponseEntity<ReturnDTO> getValidReturnRequest() {
        try {
            ReturnDTO validReturn = testDataService.generateValidReturnRequest();
            return ResponseEntity.ok(validReturn);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
