package com.example.backend.service;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
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

  // Excepción de validación simple para mapear a 400 con { message, errors }
  public static class ValidationException extends RuntimeException {
    public final Map<String, String> fieldErrors;
    public ValidationException(Map<String, String> fieldErrors) {
      super("Validation failed");
      this.fieldErrors = fieldErrors;
    }
  }
}
