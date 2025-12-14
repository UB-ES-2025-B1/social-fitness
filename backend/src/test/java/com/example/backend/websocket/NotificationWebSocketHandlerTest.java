package com.example.backend.websocket;

import com.example.backend.model.User;
import com.example.backend.service.AuthService;
import com.example.backend.service.WebSocketNotificationService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

class NotificationWebSocketHandlerTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void afterConnectionEstablished_registersSession_whenAuthenticated() throws Exception {
    WebSocketNotificationService wsService = mock(WebSocketNotificationService.class);
    AuthService authService = mock(AuthService.class);
    NotificationWebSocketHandler handler = new NotificationWebSocketHandler(wsService, authService);

    User user = new User("alice", "a@b.com", "p");
    user.setId(42L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(user);

    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(new UsernamePasswordAuthenticationToken("alice", "x", java.util.List.of()));
    SecurityContextHolder.setContext(ctx);

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.getAttributes()).thenReturn(new HashMap<>());

    handler.afterConnectionEstablished(session);

    verify(wsService).registerSession(42L, session);
    verify(session, never()).close(any());
  }

  @Test
  void afterConnectionEstablished_usesHttpSessionFallback_whenNoSecurityContext() throws Exception {
    WebSocketNotificationService wsService = mock(WebSocketNotificationService.class);
    AuthService authService = mock(AuthService.class);
    NotificationWebSocketHandler handler = new NotificationWebSocketHandler(wsService, authService);

    WebSocketSession session = mock(WebSocketSession.class);
    HttpSession httpSession = mock(HttpSession.class);
    when(httpSession.getAttribute("userId")).thenReturn("7");

    Map<String, Object> attrs = new HashMap<>();
    attrs.put("httpSession", httpSession);
    when(session.getAttributes()).thenReturn(attrs);

    handler.afterConnectionEstablished(session);

    verify(wsService).registerSession(7L, session);
    verify(session, never()).close(any());
  }

  @Test
  void afterConnectionEstablished_closesConnection_whenNoUserId() throws Exception {
    WebSocketNotificationService wsService = mock(WebSocketNotificationService.class);
    AuthService authService = mock(AuthService.class);
    NotificationWebSocketHandler handler = new NotificationWebSocketHandler(wsService, authService);

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.getAttributes()).thenReturn(new HashMap<>());

    handler.afterConnectionEstablished(session);

    verify(wsService, never()).registerSession(anyLong(), any());
    verify(session).close(any(CloseStatus.class));
  }

  @Test
  void afterConnectionClosed_unregistersSession_whenUserIdResolved() throws Exception {
    WebSocketNotificationService wsService = mock(WebSocketNotificationService.class);
    AuthService authService = mock(AuthService.class);
    NotificationWebSocketHandler handler = new NotificationWebSocketHandler(wsService, authService);

    User user = new User("alice", "a@b.com", "p");
    user.setId(42L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(user);

    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(new UsernamePasswordAuthenticationToken("alice", "x", java.util.List.of()));
    SecurityContextHolder.setContext(ctx);

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.getAttributes()).thenReturn(new HashMap<>());

    handler.afterConnectionClosed(session, CloseStatus.NORMAL);

    verify(wsService).unregisterSession(42L, session);
  }
}

