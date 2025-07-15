package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.websocket.UpdateWebSocketHandler;
import com.hamza.salesmanagementbackend.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for update system
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final UpdateWebSocketHandler updateWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handlers for update system");
        
        // Register update WebSocket handler
        registry.addHandler(updateWebSocketHandler, ApplicationConstants.WS_UPDATES_ENDPOINT)
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins(getAllowedOriginsArray())
                .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSocket

        log.info("WebSocket handlers registered successfully");
    }

    /**
     * Parse allowed origins from configuration
     */
    private String[] getAllowedOriginsArray() {
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            return new String[]{"*"};
        }
        
        return allowedOrigins.split(",");
    }
}
