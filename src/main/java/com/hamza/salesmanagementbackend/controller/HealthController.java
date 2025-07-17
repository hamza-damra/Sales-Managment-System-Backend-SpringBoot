package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.service.KeepAliveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring and keep-alive functionality
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final KeepAliveService keepAliveService;

    /**
     * Basic health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("service", "Sales Management Backend");
        response.put("version", "1.0.0");
        
        log.debug("Health check requested at {}", response.get("timestamp"));
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check with keep-alive service status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("service", "Sales Management Backend");
        response.put("version", "1.0.0");
        response.put("keepAliveService", keepAliveService.getStatus());
        
        // Add system info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        response.put("system", systemInfo);
        
        log.debug("Detailed health check requested at {}", response.get("timestamp"));
        return ResponseEntity.ok(response);
    }

    /**
     * Manual trigger for keep-alive ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> manualPing() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        try {
            keepAliveService.manualPing();
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Manual keep-alive ping triggered");
            response.put("timestamp", timestamp);
            
            log.info("Manual keep-alive ping triggered via API at {}", timestamp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to trigger keep-alive ping: " + e.getMessage());
            response.put("timestamp", timestamp);
            
            log.error("Failed to trigger manual keep-alive ping at {}: {}", timestamp, e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Simple alive endpoint for external monitoring
     */
    @GetMapping("/alive")
    public ResponseEntity<String> alive() {
        return ResponseEntity.ok("ALIVE");
    }
}
