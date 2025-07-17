package com.hamza.salesmanagementbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to keep the Render.com application alive by making periodic health check requests
 * This prevents the service from going to sleep due to inactivity on free tier
 */
@Service
@Slf4j
public class KeepAliveService {

    private final RestTemplate restTemplate;
    
    @Value("${app.keep-alive.enabled:true}")
    private boolean keepAliveEnabled;
    
    @Value("${app.keep-alive.url:}")
    private String keepAliveUrl;
    
    @Value("${app.keep-alive.interval:840000}") // 14 minutes in milliseconds
    private long keepAliveInterval;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    private static final String HEALTH_ENDPOINT = "/actuator/health";
    private static final String FALLBACK_ENDPOINT = "/api/auth/test";
    
    public KeepAliveService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    @PostConstruct
    public void init() {
        if (keepAliveEnabled) {
            // Determine the keep-alive URL if not explicitly set
            if (keepAliveUrl == null || keepAliveUrl.trim().isEmpty()) {
                // Try to detect if we're running on Render.com
                String renderUrl = System.getenv("RENDER_EXTERNAL_URL");
                if (renderUrl != null && !renderUrl.trim().isEmpty()) {
                    keepAliveUrl = renderUrl;
                    log.info("Detected Render.com deployment, using URL: {}", keepAliveUrl);
                } else {
                    // Fallback to localhost for development
                    keepAliveUrl = "http://localhost:" + serverPort;
                    log.info("Using localhost URL for keep-alive: {}", keepAliveUrl);
                }
            }
            
            log.info("Keep-alive service initialized:");
            log.info("  - Enabled: {}", keepAliveEnabled);
            log.info("  - URL: {}", keepAliveUrl);
            log.info("  - Interval: {} minutes", keepAliveInterval / 60000);
            log.info("  - Next ping in {} minutes", keepAliveInterval / 60000);
        } else {
            log.info("Keep-alive service is disabled");
        }
    }
    
    /**
     * Scheduled method to ping the service every 14 minutes (before the 15-minute sleep timeout)
     * Uses @Async to prevent blocking the scheduler thread
     */
    @Scheduled(fixedRateString = "${app.keep-alive.interval:840000}") // 14 minutes
    @Async
    public void pingService() {
        if (!keepAliveEnabled) {
            return;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.debug("Keep-alive ping started at {}", timestamp);
        
        try {
            // Try health endpoint first
            String healthUrl = keepAliveUrl + HEALTH_ENDPOINT;
            ResponseEntity<String> response = makeRequest(healthUrl);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Keep-alive ping successful to {} at {} - Status: {}", 
                        healthUrl, timestamp, response.getStatusCode());
                return;
            }
            
            // Fallback to test endpoint
            String fallbackUrl = keepAliveUrl + FALLBACK_ENDPOINT;
            response = makeRequest(fallbackUrl);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Keep-alive ping successful to {} at {} - Status: {}", 
                        fallbackUrl, timestamp, response.getStatusCode());
            } else {
                log.warn("Keep-alive ping returned non-success status: {} for URL: {}", 
                        response.getStatusCode(), fallbackUrl);
            }
            
        } catch (Exception e) {
            log.error("Keep-alive ping failed at {}: {}", timestamp, e.getMessage());
            // Don't throw exception to prevent scheduler from stopping
        }
    }
    
    /**
     * Make HTTP request with error handling
     */
    private ResponseEntity<String> makeRequest(String url) {
        try {
            log.debug("Making keep-alive request to: {}", url);
            return restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            log.debug("Request failed for {}: {}", url, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Manual ping method for testing or immediate wake-up
     */
    public void manualPing() {
        log.info("Manual keep-alive ping triggered");
        pingService();
    }
    
    /**
     * Get keep-alive service status
     */
    public String getStatus() {
        return String.format("Keep-alive service - Enabled: %s, URL: %s, Interval: %d minutes", 
                keepAliveEnabled, keepAliveUrl, keepAliveInterval / 60000);
    }
}
