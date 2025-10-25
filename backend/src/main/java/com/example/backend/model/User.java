package com.example.backend.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;   
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
  }
)
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String username;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  // Almacenar como texto para evitar problemas de tipo al registrar usuarios
  @Column
  private String sports; // JSON serializado como String

  @Column(length = 500)
  private String bio;

  @Column(name = "profile_image")
  private String profileImage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
  public User() {}

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }
  public String getSports() { return sports; }
  public String getBio() { return bio; }
  public String getProfileImage() { return profileImage; }
  public LocalDateTime getCreatedAt() { return createdAt; }


  public void setId(Long id) { this.id = id; }
  public void setUsername(String username) { this.username = username; }
  public void setEmail(String email) { this.email = email; }
  public void setPassword(String password) { this.password = password; }
  public void setSports(String sports) { this.sports = sports; }
  public void setBio(String bio) { this.bio = bio; }
  public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  // Métodos requeridos por UserDetails
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList(); 
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
