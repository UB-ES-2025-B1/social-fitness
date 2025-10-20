package com.example.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "events")
public class Event {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false) private String title;
  @Column(nullable = false) private String sport;       // p.ej. "football"
  @Column(nullable = false) private LocalDate date;     // 2025-10-18
  @Column(nullable = false) private LocalTime time;     // 18:00
  @Column(nullable = false) private String location;
  @Column(nullable = false) private String organizer;   // simplificado: nombre
  @Column(nullable = false) private Integer capacity = 10;
  @Column(nullable = false) private Integer participants = 0;
  @Column(nullable = false) private BigDecimal price = BigDecimal.ZERO;
  private String image;                                 // URL opcional
  @Column(columnDefinition = "text") private String description;

  // getters/setters...
  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getSport() { return sport; }
  public LocalDate getDate() { return date; }
  public LocalTime getTime() { return time; }
  public String getLocation() { return location; }
  public String getOrganizer() { return organizer; }
  public Integer getCapacity() { return capacity; }
  public Integer getParticipants() { return participants; }
  public BigDecimal getPrice() { return price; }
  public String getImage() { return image; }
  public String getDescription() { return description; }

  public void setId(Long id) { this.id = id; }
  public void setTitle(String title) { this.title = title; }
  public void setSport(String sport) { this.sport = sport; }
  public void setDate(LocalDate date) { this.date = date; }
  public void setTime(LocalTime time) { this.time = time; }
  public void setLocation(String location) { this.location = location; }
  public void setOrganizer(String organizer) { this.organizer = organizer; }
  public void setCapacity(Integer capacity) { this.capacity = capacity; }
  public void setParticipants(Integer participants) { this.participants = participants; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public void setImage(String image) { this.image = image; }
  public void setDescription(String description) { this.description = description; }
}
