package com.example.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_read", columnList = "user_id, read_flag"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(name = "read_flag", nullable = false)
    private Boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    public Notification() {
        this.createdAt = Instant.now();
    }

    public Notification(Long userId, NotificationType type, String title, String message, Long eventId, Long relatedUserId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.eventId = eventId;
        this.relatedUserId = relatedUserId;
        this.read = false;
        this.createdAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Long relatedUserId) { this.relatedUserId = relatedUserId; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public enum NotificationType {
        EVENT_STARTING,
        NEW_MESSAGE,
        JOINED_EVENT,
        NEW_PARTICIPANT,
        EVENT_CANCELLED,
        EVENT_UPDATED
    }
}