package com.example.backend.dto;
import java.time.LocalDateTime;   

public class UserResponse {
  private String id;
  private String username;
  private String email;
  private String sports;
  private String bio;
  private String profileImage;
  private LocalDateTime createdAt;

  public UserResponse(String id, String username, String email) {
    this.id = id; this.username = username; this.email = email;
  }

  public String getId() { return id; }
  
  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getSports() { return sports; }
  public String getBio() { return bio; }
  public String getProfileImage() { return profileImage; }
  public LocalDateTime getCreatedAt() { return createdAt; }


  public void setId(String id) { this.id = id; }
  public void setUsername(String username) { this.username = username; }
  public void setEmail(String email) { this.email = email; }
  public void setSports(String sports) { this.sports = sports; }
  public void setBio(String bio) { this.bio = bio; }
  public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

