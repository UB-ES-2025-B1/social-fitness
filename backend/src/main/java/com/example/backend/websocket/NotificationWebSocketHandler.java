package com.example.backend.websocket;

import com.example.backend.service.AuthService;
import com.example.backend.service.WebSocketNotificationService;
import com.example.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
        // Obtener usuario autenticado desde la sesión HTTP
        Authentication auth = (Authentication) session.getPrincipal();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
            return;
        }

        try {
            User user = authService.getCurrentAuthenticatedUser();
            wsService.registerSession(user.getId(), session);
            System.out.println("WebSocket connected for user: " + user.getId());
        } catch (Exception e) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Authentication auth = (Authentication) session.getPrincipal();
        if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
            try {
                User user = authService.getCurrentAuthenticatedUser();
                wsService.unregisterSession(user.getId(), session);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}