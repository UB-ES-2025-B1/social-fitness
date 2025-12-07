package com.example.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "direct_messages")
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false)
    private boolean read = false;

    // Getters y setters
    public String getId() { return id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
