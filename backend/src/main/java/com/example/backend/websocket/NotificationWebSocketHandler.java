package com.example.backend.websocket;

import com.example.backend.service.AuthService;
import com.example.backend.service.WebSocketNotificationService;
import com.example.backend.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketNotificationService wsService;
    private final AuthService authService;

    public NotificationWebSocketHandler(WebSocketNotificationService wsService, AuthService authService) {
        this.wsService = wsService;
        this.authService = authService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Obtener la sesión HTTP desde los atributos del WebSocket
        Long userId = extractUserIdFromSession(session);
        
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
            return;
        }

        wsService.registerSession(userId, session);
        System.out.println("WebSocket connected for user: " + userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserIdFromSession(session);
        if (userId != null) {
            wsService.unregisterSession(userId, session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // No procesamos mensajes entrantes en este momento
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket error: " + exception.getMessage());
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        try {
            // Intentar obtener userId del contexto de seguridad
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
                User user = authService.getCurrentAuthenticatedUser();
                return user.getId();
            }
        } catch (Exception e) {
            // Ignorar y usar atributos de sesión como fallback
        }

        // Fallback: obtener userId de los atributos de la sesión HTTP
        try {
            HttpSession httpSession = (HttpSession) session.getAttributes().get("httpSession");
            if (httpSession != null) {
                Object userId = httpSession.getAttribute("userId");
                if (userId instanceof Long) return (Long) userId;
                if (userId instanceof String) return Long.parseLong((String) userId);
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }
}