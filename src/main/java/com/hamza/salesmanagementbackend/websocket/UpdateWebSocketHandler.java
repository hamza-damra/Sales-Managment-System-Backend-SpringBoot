package com.hamza.salesmanagementbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.service.RateLimitingService;
import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import com.hamza.salesmanagementbackend.entity.ConnectedClient;
import com.hamza.salesmanagementbackend.repository.ConnectedClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for update notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateWebSocketHandler implements WebSocketHandler {

    private final ConnectedClientRepository connectedClientRepository;
    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    @Value("${app.updates.websocket.heartbeat-interval:30000}")
    private long heartbeatInterval;

    @Value("${app.updates.websocket.connection-timeout:300000}")
    private long connectionTimeout;

    // Store active WebSocket sessions
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, ConnectedClient> sessionClients = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        try {
            // Check rate limiting
            String clientId = getClientIdentifier(session);
            String clientIp = getClientIp(session);
            
            RateLimitingService.RateLimitResult rateLimitResult = rateLimitingService
                .checkRateLimit(clientId, clientIp, RateLimitTracker.EndpointType.WEBSOCKET);

            if (!rateLimitResult.isAllowed()) {
                log.warn("WebSocket connection rate limited for client: {}", clientId);
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Rate limit exceeded"));
                return;
            }

            // Store session
            activeSessions.put(session.getId(), session);

            // Send welcome message
            sendMessage(session, new WebSocketMessage("WELCOME", Map.of(
                "sessionId", session.getId(),
                "serverTime", LocalDateTime.now().toString(),
                "heartbeatInterval", heartbeatInterval
            )));

            log.info("WebSocket connection established successfully for session: {}", session.getId());

        } catch (Exception e) {
            log.error("Error establishing WebSocket connection", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
        log.debug("Received WebSocket message from session {}: {}", session.getId(), message);

        try {
            if (message instanceof TextMessage) {
                handleTextMessage(session, (TextMessage) message);
            } else if (message instanceof PongMessage) {
                handlePongMessage(session, (PongMessage) message);
            } else {
                log.warn("Unsupported message type: {}", message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            sendErrorMessage(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        
        // Clean up session
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed for session {}: {}", session.getId(), closeStatus);
        
        // Clean up session
        cleanupSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Handle text messages from clients
     */
    private void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        
        try {
            WebSocketMessage clientMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            String messageType = clientMessage.getType();

            switch (messageType) {
                case "REGISTER":
                    handleClientRegistration(session, clientMessage);
                    break;
                case "PING":
                    handlePingMessage(session, clientMessage);
                    break;
                case "SUBSCRIBE":
                    handleChannelSubscription(session, clientMessage);
                    break;
                case "UNSUBSCRIBE":
                    handleChannelUnsubscription(session, clientMessage);
                    break;
                default:
                    log.warn("Unknown message type: {}", messageType);
                    sendErrorMessage(session, "Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            log.error("Error parsing WebSocket message: {}", payload, e);
            sendErrorMessage(session, "Invalid message format");
        }
    }

    /**
     * Handle client registration
     */
    private void handleClientRegistration(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            
            String clientVersion = (String) data.get("clientVersion");
            String clientId = (String) data.get("clientId");
            
            if (clientId == null) {
                clientId = getClientIdentifier(session);
            }

            // Create or update connected client record
            ConnectedClient connectedClient = ConnectedClient.builder()
                .sessionId(session.getId())
                .clientVersion(clientVersion)
                .clientIp(getClientIp(session))
                .connectedAt(LocalDateTime.now())
                .lastPingAt(LocalDateTime.now())
                .isActive(true)
                .build();

            connectedClient = connectedClientRepository.save(connectedClient);
            sessionClients.put(session.getId(), connectedClient);

            // Send registration confirmation
            sendMessage(session, new WebSocketMessage("REGISTERED", Map.of(
                "clientId", clientId,
                "sessionId", session.getId(),
                "status", "connected"
            )));

            log.info("Client registered: {} (version: {}, session: {})", clientId, clientVersion, session.getId());

        } catch (Exception e) {
            log.error("Error handling client registration", e);
            sendErrorMessage(session, "Registration failed: " + e.getMessage());
        }
    }

    /**
     * Handle ping messages
     */
    private void handlePingMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            // Update last ping time
            ConnectedClient client = sessionClients.get(session.getId());
            if (client != null) {
                client.updateLastPing();
                connectedClientRepository.save(client);
            }

            // Send pong response
            sendMessage(session, new WebSocketMessage("PONG", Map.of(
                "timestamp", LocalDateTime.now().toString()
            )));

        } catch (Exception e) {
            log.error("Error handling ping message", e);
        }
    }

    /**
     * Handle channel subscription
     */
    private void handleChannelSubscription(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String channel = (String) data.get("channel");

            // Store subscription in session attributes
            session.getAttributes().put("subscribedChannel", channel);

            sendMessage(session, new WebSocketMessage("SUBSCRIBED", Map.of(
                "channel", channel,
                "status", "subscribed"
            )));

            log.info("Client subscribed to channel: {} (session: {})", channel, session.getId());

        } catch (Exception e) {
            log.error("Error handling channel subscription", e);
            sendErrorMessage(session, "Subscription failed: " + e.getMessage());
        }
    }

    /**
     * Handle channel unsubscription
     */
    private void handleChannelUnsubscription(WebSocketSession session, WebSocketMessage message) {
        try {
            session.getAttributes().remove("subscribedChannel");

            sendMessage(session, new WebSocketMessage("UNSUBSCRIBED", Map.of(
                "status", "unsubscribed"
            )));

            log.info("Client unsubscribed from channel (session: {})", session.getId());

        } catch (Exception e) {
            log.error("Error handling channel unsubscription", e);
        }
    }

    /**
     * Handle pong messages (response to server ping)
     */
    private void handlePongMessage(WebSocketSession session, PongMessage message) {
        log.debug("Received pong from session: {}", session.getId());
        
        // Update last ping time
        ConnectedClient client = sessionClients.get(session.getId());
        if (client != null) {
            client.updateLastPing();
            connectedClientRepository.save(client);
        }
    }

    /**
     * Send message to specific session
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.error("Error sending WebSocket message to session {}", session.getId(), e);
        }
    }

    /**
     * Send error message to session
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        sendMessage(session, new WebSocketMessage("ERROR", Map.of(
            "message", errorMessage,
            "timestamp", LocalDateTime.now().toString()
        )));
    }

    /**
     * Broadcast message to all connected clients
     */
    public void broadcastMessage(WebSocketMessage message) {
        log.info("Broadcasting message to {} connected clients", activeSessions.size());
        
        activeSessions.values().parallelStream().forEach(session -> {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                log.error("Error broadcasting to session {}", session.getId(), e);
            }
        });
    }

    /**
     * Broadcast message to clients subscribed to specific channel
     */
    public void broadcastToChannel(String channel, WebSocketMessage message) {
        log.info("Broadcasting message to channel '{}' subscribers", channel);
        
        activeSessions.values().parallelStream()
            .filter(session -> channel.equals(session.getAttributes().get("subscribedChannel")))
            .forEach(session -> {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("Error broadcasting to session {}", session.getId(), e);
                }
            });
    }

    /**
     * Clean up session resources
     */
    private void cleanupSession(WebSocketSession session) {
        try {
            // Remove from active sessions
            activeSessions.remove(session.getId());

            // Update database record
            ConnectedClient client = sessionClients.remove(session.getId());
            if (client != null) {
                client.disconnect();
                connectedClientRepository.save(client);
            }

            log.debug("Session cleanup completed for: {}", session.getId());

        } catch (Exception e) {
            log.error("Error during session cleanup", e);
        }
    }

    /**
     * Get client identifier from session
     */
    private String getClientIdentifier(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");
        return username != null ? username : session.getId();
    }

    /**
     * Get client IP from session
     */
    private String getClientIp(WebSocketSession session) {
        String clientIp = (String) session.getAttributes().get("clientIp");
        return clientIp != null ? clientIp : "unknown";
    }

    /**
     * Send heartbeat to all connected clients (scheduled task)
     */
    @Scheduled(fixedRateString = "${app.updates.websocket.heartbeat-interval:30000}")
    public void sendHeartbeat() {
        if (!activeSessions.isEmpty()) {
            log.debug("Sending heartbeat to {} connected clients", activeSessions.size());
            
            WebSocketMessage heartbeat = new WebSocketMessage("HEARTBEAT", Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "connectedClients", activeSessions.size()
            ));

            activeSessions.values().parallelStream().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        // Send both the heartbeat message and a ping
                        sendMessage(session, heartbeat);
                        session.sendMessage(new PingMessage());
                    }
                } catch (Exception e) {
                    log.warn("Error sending heartbeat to session {}", session.getId(), e);
                    // Remove problematic session
                    cleanupSession(session);
                }
            });
        }
    }

    /**
     * Clean up stale connections (scheduled task)
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupStaleConnections() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minus(connectionTimeout, ChronoUnit.MILLIS);
            
            // Find stale sessions
            activeSessions.entrySet().removeIf(entry -> {
                WebSocketSession session = entry.getValue();
                ConnectedClient client = sessionClients.get(session.getId());
                
                if (client != null && client.getLastPingAt().isBefore(cutoff)) {
                    log.info("Removing stale WebSocket connection: {}", session.getId());
                    try {
                        if (session.isOpen()) {
                            session.close(CloseStatus.GOING_AWAY.withReason("Connection timeout"));
                        }
                    } catch (Exception e) {
                        log.warn("Error closing stale session", e);
                    }
                    cleanupSession(session);
                    return true;
                }
                
                return false;
            });

        } catch (Exception e) {
            log.error("Error during stale connection cleanup", e);
        }
    }

    /**
     * Get count of active connections
     */
    public int getActiveConnectionCount() {
        return activeSessions.size();
    }

    /**
     * WebSocket message class
     */
    public static class WebSocketMessage {
        private String type;
        private Object data;

        public WebSocketMessage() {}

        public WebSocketMessage(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
