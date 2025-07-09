package com.hamza.salesmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for caching report data to improve performance
 * In production, this should be replaced with Redis or another distributed cache
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportCacheService {

    // In-memory cache for demonstration - replace with Redis in production
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    /**
     * Cache a report with expiration time in minutes
     */
    public void cacheReport(String key, Object data, int expirationMinutes) {
        log.debug("Caching report with key: {} for {} minutes", key, expirationMinutes);
        
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(expirationMinutes);
        CacheEntry entry = new CacheEntry(data, expiry);
        cache.put(key, entry);
        
        // Clean up expired entries periodically
        cleanupExpiredEntries();
    }
    
    /**
     * Retrieve cached report data
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedReport(String key, Class<T> type) {
        log.debug("Retrieving cached report with key: {}", key);
        
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            log.debug("Cache miss for key: {}", key);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("Cache entry expired for key: {}", key);
            cache.remove(key);
            return null;
        }
        
        log.debug("Cache hit for key: {}", key);
        return (T) entry.getData();
    }
    
    /**
     * Invalidate cached report
     */
    public void invalidateCache(String key) {
        log.debug("Invalidating cache for key: {}", key);
        cache.remove(key);
    }
    
    /**
     * Clear all cached reports
     */
    public void clearAllCache() {
        log.info("Clearing all cached reports");
        cache.clear();
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        long totalEntries = cache.size();
        long expiredEntries = cache.values().stream()
                .mapToLong(entry -> entry.isExpired() ? 1 : 0)
                .sum();
        
        return new CacheStats(totalEntries, expiredEntries, totalEntries - expiredEntries);
    }
    
    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Cache entry wrapper
     */
    private static class CacheEntry {
        private final Object data;
        private final LocalDateTime expiry;
        
        public CacheEntry(Object data, LocalDateTime expiry) {
            this.data = data;
            this.expiry = expiry;
        }
        
        public Object getData() {
            return data;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }
    
    /**
     * Cache statistics
     */
    public static class CacheStats {
        private final long totalEntries;
        private final long expiredEntries;
        private final long activeEntries;
        
        public CacheStats(long totalEntries, long expiredEntries, long activeEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
            this.activeEntries = activeEntries;
        }
        
        public long getTotalEntries() { return totalEntries; }
        public long getExpiredEntries() { return expiredEntries; }
        public long getActiveEntries() { return activeEntries; }
    }
}
