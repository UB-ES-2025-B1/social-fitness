package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.AuthService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {
    UserResponse user = authService.register(body);
    // contrato: 201 { "user": {...} }
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("user", user));
  }

@PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    UserResponse user = authService.login(body);
    // Ensure the SecurityContext is stored in the HTTP session so the browser receives JSESSIONID
    try {
      var session = request.getSession(true);
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    } catch (Exception ignore) {// ignore session issues
    }
    return ResponseEntity.ok(Map.of("user", user, "message", "Login successful"));
  }
}
