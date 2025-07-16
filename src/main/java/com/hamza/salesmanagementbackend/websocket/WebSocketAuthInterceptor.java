package com.hamza.salesmanagementbackend.websocket;

import com.hamza.salesmanagementbackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket authentication interceptor
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                 @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        
        log.debug("WebSocket handshake attempt from: {}", request.getRemoteAddress());

        try {
            // Extract JWT token from query parameter or header
            String token = extractToken(request);
            
            if (token == null) {
                log.warn("WebSocket connection rejected: No authentication token provided");
                return false;
            }

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket connection rejected: Invalid authentication token");
                return false;
            }

            // Extract user information from token
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String userId = jwtTokenProvider.getUserIdFromToken(token);

            // Store authentication information in WebSocket session attributes
            attributes.put("authenticated", true);
            attributes.put("username", username);
            attributes.put("userId", userId);
            attributes.put("token", token);
            attributes.put("clientIp", getClientIpAddress(request));

            log.info("WebSocket connection authenticated for user: {} (ID: {})", username, userId);
            return true;

        } catch (Exception e) {
            log.error("Error during WebSocket authentication", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                             @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        
        if (exception != null) {
            log.error("WebSocket handshake failed", exception);
        } else {
            log.debug("WebSocket handshake completed successfully");
        }
    }

    /**
     * Extract JWT token from request
     */
    private String extractToken(ServerHttpRequest request) {
        // Try to get token from query parameter first
        URI uri = request.getURI();
        String query = uri.getQuery();
        
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }

        // Try to get token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Try to get token from custom header
        String tokenHeader = request.getHeaders().getFirst("X-Auth-Token");
        if (tokenHeader != null) {
            return tokenHeader;
        }

        return null;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        // Check for X-Forwarded-For header (proxy/load balancer)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header (nginx)
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddress() != null ? 
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
