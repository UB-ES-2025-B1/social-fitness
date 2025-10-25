package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_sports")
public class UserSport {
    public enum Level {
        PRINCIPIANTE, INTERMEDIO, AVANZADO, EXPERTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
}

