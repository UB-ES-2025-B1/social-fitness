package com.example.backend.service;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.model.Notification;
import com.example.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final WebSocketNotificationService wsService;

    public NotificationService(NotificationRepository repo, WebSocketNotificationService wsService) {
        this.repo = repo;
        this.wsService = wsService;
    }

    @Transactional
    public void createNotification(Long userId, Notification.NotificationType type, String title, String message, Long eventId, Long relatedUserId) {
        Notification n = new Notification(userId, type, title, message, eventId, relatedUserId);
        Notification saved = repo.save(n);
        // Send via WebSocket
        wsService.notifyUser(userId, saved);
    }

    public List<NotificationDTO> getNotifications(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationDTO::new)
            .collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        return repo.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) throws IllegalAccessException {
        Notification n = repo.findById(notificationId)
            .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        
        if (!n.getUserId().equals(userId)) {
            throw new IllegalAccessException("Not authorized to access this notification");
        }
        
        n.setRead(true);
        repo.save(n);
    }

    @Transactional
    public long markAllAsRead(Long userId) {
        List<Notification> unread = repo.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .filter(n -> !n.getRead())
            .collect(Collectors.toList());
        
        long count = unread.size();
        unread.forEach(n -> n.setRead(true));
        repo.saveAll(unread);
        return count;
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) throws IllegalAccessException {
        Notification n = repo.findById(notificationId)
            .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        
        if (!n.getUserId().equals(userId)) {
            throw new IllegalAccessException("Not authorized to delete this notification");
        }
        
        repo.delete(n);
    }

    // Helpers para crear notificaciones específicas
    public void notifyJoinedEvent(Long userId, Long eventId, String eventTitle) {
        createNotification(userId, Notification.NotificationType.JOINED_EVENT, 
            "Te has unido", 
            "Te has unido al evento '" + eventTitle + "'", 
            eventId, null);
    }

    public void notifyNewParticipant(Long organizerId, Long eventId, String eventTitle, Long participantId) {
        createNotification(organizerId, Notification.NotificationType.NEW_PARTICIPANT, 
            "Nuevo participante", 
            "Un nuevo participante se ha unido a tu evento '" + eventTitle + "'", 
            eventId, participantId);
    }

    public void notifyEventCancelled(Long userId, Long eventId, String eventTitle) {
        createNotification(userId, Notification.NotificationType.EVENT_CANCELLED, 
            "Evento cancelado", 
            "El evento '" + eventTitle + "' ha sido cancelado", 
            eventId, null);
    }

    public void notifyEventUpdated(Long userId, Long eventId, String eventTitle) {
        createNotification(userId, Notification.NotificationType.EVENT_UPDATED, 
            "Evento actualizado", 
            "El evento '" + eventTitle + "' ha sido actualizado", 
            eventId, null);
    }

    public void notifyEventStarting(Long userId, Long eventId, String eventTitle) {
        createNotification(userId, Notification.NotificationType.EVENT_STARTING, 
            "Evento comenzando", 
            "Tu evento '" + eventTitle + "' comienza en 1 hora", 
            eventId, null);
    }

    public void notifyNewMessage(Long recipientId, String senderUsername) {
        createNotification(recipientId, Notification.NotificationType.NEW_MESSAGE, 
            "Nuevo mensaje", 
            "Tienes un nuevo mensaje de " + senderUsername, 
            null, null);
    }
}