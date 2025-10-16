package com.example.backend.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
  @NotBlank @Size(min = 3, max = 30)
  private String username;

  @NotBlank @Email
  private String email;

  @NotBlank @Size(min = 8, message = "Password too short")
  private String password;

  public String getUsername() { return username; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }

  public void setUsername(String username) { this.username = username; }
  public void setEmail(String email) { this.email = email; }
  public void setPassword(String password) { this.password = password; }
}
