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

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        User user = authService.getCurrentAuthenticatedUser();
        List<NotificationDTO> notifications = notificationService.getNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User user = authService.getCurrentAuthenticatedUser();
        Long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        User user = authService.getCurrentAuthenticatedUser();
        try {
            notificationService.markAsRead(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(Map.of("message", "Not authorized to access this notification"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", "Notification not found"));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        User user = authService.getCurrentAuthenticatedUser();
        long count = notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of(
            "message", "All notifications marked as read",
            "count", count
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        User user = authService.getCurrentAuthenticatedUser();
        try {
            notificationService.deleteNotification(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Notification deleted"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(403).body(Map.of("message", "Not authorized to delete this notification"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", "Notification not found"));
        }
    }
}