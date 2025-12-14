package com.example.backend.service;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.model.Notification;
import com.example.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  NotificationRepository repo;

  @Mock
  WebSocketNotificationService wsService;

  @InjectMocks
  NotificationService service;

  @Test
  void createNotification_saves_andNotifiesViaWebSocket() {
    Notification saved = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "t", "m", null, null);
    saved.setId(55L);
    when(repo.save(any(Notification.class))).thenReturn(saved);

    service.createNotification(1L, Notification.NotificationType.NEW_MESSAGE, "t", "m", null, null);

    verify(repo).save(any(Notification.class));
    verify(wsService).notifyUser(1L, saved);
  }

  @Test
  void getNotifications_mapsEntitiesToDTOs() {
    Notification n = new Notification(1L, Notification.NotificationType.NEW_MESSAGE, "Title", "Msg", 10L, 20L);
    n.setId(1L);
    n.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
    when(repo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(n));

    List<NotificationDTO> dtos = service.getNotifications(1L);

    assertEquals(1, dtos.size());
    assertEquals("1", dtos.get(0).getId());
    assertEquals("NEW_MESSAGE", dtos.get(0).getType());
    assertEquals("10", dtos.get(0).getEventId());
    assertEquals("20", dtos.get(0).getRelatedUserId());
  }

  @Test
  void markAsRead_throws_whenNotificationMissing() {
    when(repo.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NoSuchElementException.class, () -> service.markAsRead(1L, 1L));
  }

  @Test
  void markAsRead_throwsIllegalAccess_whenUserDoesNotOwnNotification() {
    Notification n = new Notification();
    n.setId(1L);
    n.setUserId(99L);
    when(repo.findById(1L)).thenReturn(Optional.of(n));

    assertThrows(IllegalAccessException.class, () -> service.markAsRead(1L, 1L));
    verify(repo, never()).save(any());
  }

  @Test
  void markAsRead_setsReadTrue_andSaves_whenAuthorized() throws Exception {
    Notification n = new Notification();
    n.setId(1L);
    n.setUserId(1L);
    n.setRead(false);
    when(repo.findById(1L)).thenReturn(Optional.of(n));

    service.markAsRead(1L, 1L);

    assertTrue(n.getRead());
    verify(repo).save(n);
  }

  @Test
  void markAllAsRead_marksOnlyUnread_andReturnsCount() {
    Notification unread1 = new Notification();
    unread1.setId(1L);
    unread1.setUserId(1L);
    unread1.setRead(false);

    Notification unread2 = new Notification();
    unread2.setId(2L);
    unread2.setUserId(1L);
    unread2.setRead(false);

    Notification alreadyRead = new Notification();
    alreadyRead.setId(3L);
    alreadyRead.setUserId(1L);
    alreadyRead.setRead(true);

    when(repo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(unread1, alreadyRead, unread2));

    long count = service.markAllAsRead(1L);

    assertEquals(2L, count);
    assertTrue(unread1.getRead());
    assertTrue(unread2.getRead());
    assertTrue(alreadyRead.getRead());

    ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
    verify(repo).saveAll(captor.capture());
    assertEquals(2, captor.getValue().size());
  }

  @Test
  void deleteNotification_deletes_whenAuthorized() throws Exception {
    Notification n = new Notification();
    n.setId(1L);
    n.setUserId(1L);
    when(repo.findById(1L)).thenReturn(Optional.of(n));

    service.deleteNotification(1L, 1L);

    verify(repo).delete(n);
  }

  @Test
  void notifyNewMessage_createsNotification_andNotifiesWebSocket() {
    when(repo.save(any(Notification.class))).thenAnswer(inv -> {
      Notification n = inv.getArgument(0, Notification.class);
      n.setId(123L);
      return n;
    });

    service.notifyNewMessage(1L, "alice");

    verify(wsService).notifyUser(eq(1L), any(Notification.class));
  }
}

