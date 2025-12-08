package com.example.backend.dto;

import com.example.backend.model.Notification;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    private String id;
    private String type;
    private String title;
    private String message;
    private String eventId;
    private String relatedUserId;
    private Boolean read;
    private String createdAt;

    public NotificationDTO(Notification n) {
        this.id = n.getId().toString();
        this.type = n.getType().toString();
        this.title = n.getTitle();
        this.message = n.getMessage();
        this.eventId = n.getEventId() != null ? n.getEventId().toString() : null;
        this.relatedUserId = n.getRelatedUserId() != null ? n.getRelatedUserId().toString() : null;
        this.read = n.getRead();
        this.createdAt = n.getCreatedAt().toString();
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getEventId() { return eventId; }
    public String getRelatedUserId() { return relatedUserId; }
    public Boolean getRead() { return read; }
    public String getCreatedAt() { return createdAt; }
}