package com.example.backend.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager; // clase de spring security

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
  }

  public UserResponse register(RegisterRequest req) {
    if (userRepository.existsByUsername(req.getUsername())) {
      throw new ValidationException(Map.of("username", "Username already taken"));
    }
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new ValidationException(Map.of("email", "Email already taken"));
    }

    User user = new User(
      req.getUsername(),
      req.getEmail(),
      passwordEncoder.encode(req.getPassword())
    );

    User saved = userRepository.save(user);
    return new UserResponse(saved.getId().toString(), saved.getUsername(), saved.getEmail());
  }

  public String login(LoginRequest req) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
      );

      // la autenticación es correcta
      return "Login successful: " + authentication.getName();
    } catch (AuthenticationException e) {
      throw new AuthenticationException("Invalid credentials") {};
    }
  }

  // Excepción de validación simple para mapear a 400 con { message, errors }
  public static class ValidationException extends RuntimeException {
    public final Map<String, String> fieldErrors;
    public ValidationException(Map<String, String> fieldErrors) {
      super("Validation failed");
      this.fieldErrors = fieldErrors;
    }
  }
}
