package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
  }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String username;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

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

  public void setId(Long id) { this.id = id; }
  public void setUsername(String username) { this.username = username; }
  public void setEmail(String email) { this.email = email; }
  public void setPassword(String password) { this.password = password; }
}
