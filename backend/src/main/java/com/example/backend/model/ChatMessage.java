package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long eventId;
    private String userId;   // Guardamos el ID como String  
    private String username; // Guardamos el nombre  
    private String text;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(Long eventId, String userId, String username, String text) {
        this.eventId = eventId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
}