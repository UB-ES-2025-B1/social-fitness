package com.example.backend.controller;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.service.AuthService;
import com.example.backend.service.NotificationService;
import com.example.backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationsController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
    }

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            List<NotificationDTO> notifications = notificationService.getNotifications(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return unauthorized();
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            Long count = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return unauthorized();
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            notificationService.markAsRead(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(Map.of("message", "Not authorized to access this notification"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", "Notification not found"));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            long count = notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read",
                "count", count
            ));
        } catch (Exception e) {
            return unauthorized();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            notificationService.deleteNotification(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification deleted"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(Map.of("message", "Not authorized to delete this notification"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", "Notification not found"));
        }
    }
}