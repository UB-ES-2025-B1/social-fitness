package com.example.backend.dto;

import jakarta.validation.constraints.*;

public class LoginRequest {
  
  @NotBlank
  private String username;  

  @NotBlank
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;

  // Getters and Setters
  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setUsername(String username) { this.username = username; }
  public void setPassword(String password) { this.password = password; }

}
