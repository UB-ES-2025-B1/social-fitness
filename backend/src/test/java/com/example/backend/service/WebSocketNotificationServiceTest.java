package com.example.backend.service;

import com.example.backend.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketNotificationServiceTest {

  @Test
  void notifyUser_doesNothing_whenNoSessionsRegistered() {
    WebSocketNotificationService service = new WebSocketNotificationService();
    Notification n = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "t", "m", null, null);
    n.setId(1L);
    n.setCreatedAt(Instant.now());

    assertDoesNotThrow(() -> service.notifyUser(1L, n));
  }

  @Test
  void notifyUser_sendsMessage_whenSessionOpen() throws Exception {
    WebSocketNotificationService service = new WebSocketNotificationService();

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    service.registerSession(1L, session);

    Notification n = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "Title", "Msg", null, null);
    n.setId(123L);
    n.setCreatedAt(Instant.now());

    service.notifyUser(1L, n);

    verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
  }

  @Test
  void notifyUser_removesSession_whenSendFails() throws Exception {
    WebSocketNotificationService service = new WebSocketNotificationService();

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);
    doThrow(new IOException("boom"))
        .doNothing()
        .when(session).sendMessage(any(TextMessage.class));

    service.registerSession(1L, session);

    Notification n = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "Title", "Msg", null, null);
    n.setId(123L);
    n.setCreatedAt(Instant.now());

    service.notifyUser(1L, n);
    service.notifyUser(1L, n);

    verify(session, times(1)).sendMessage(any(TextMessage.class));
  }

  @Test
  void notifyUnreadCount_sendsMessage_whenSessionOpen() throws Exception {
    WebSocketNotificationService service = new WebSocketNotificationService();

    WebSocketSession session = mock(WebSocketSession.class);
    when(session.isOpen()).thenReturn(true);

    service.registerSession(1L, session);
    service.notifyUnreadCount(1L, 5L);

    verify(session, atLeastOnce()).sendMessage(any(TextMessage.class));
  }

  @Test
  void unregisterSession_removesUserEntry_whenLastSessionRemoved() {
    WebSocketNotificationService service = new WebSocketNotificationService();

    WebSocketSession session = mock(WebSocketSession.class);
    service.registerSession(1L, session);
    service.unregisterSession(1L, session);

    Notification n = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "t", "m", null, null);
    n.setId(1L);
    n.setCreatedAt(Instant.now());

    assertDoesNotThrow(() -> service.notifyUser(1L, n));
  }
}

