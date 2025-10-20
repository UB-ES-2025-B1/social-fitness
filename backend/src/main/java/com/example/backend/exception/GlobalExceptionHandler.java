package com.example.backend.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.example.backend.service.AuthService.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Errores de @Valid (campos inválidos)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleBeanValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = ex.getBindingResult()
      .getFieldErrors()
      .stream()
      .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a,b)->a));
    return ResponseEntity.badRequest().body(Map.of(
      "message", "Validation failed",
      "errors", errors
    ));
  }

  // Errores de negocio (duplicados detectados en servicio)
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<?> handleBusiness(ValidationException ex) {
    return ResponseEntity.badRequest().body(Map.of(
      "message", "Validation failed",
      "errors", ex.fieldErrors
    ));
  }

  // Por si salta la UNIQUE constraint desde la BD (carrera)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleUniqueConstraint(DataIntegrityViolationException ex) {
    // Si quieres, parsea el mensaje para diferenciar email/username; aquí genérico:
    return ResponseEntity.badRequest().body(Map.of(
      "message", "Validation failed",
      "errors", Map.of("username", "Username already taken")
    ));
  }


  @ExceptionHandler(java.util.NoSuchElementException.class)
  public ResponseEntity<?> handleNotFound(RuntimeException ex) {
    return ResponseEntity.status(404).body(Map.of("message", "Not found"));
  }

  @ExceptionHandler(com.example.backend.service.EventService.EventFullException.class)
  public ResponseEntity<?> handleEventFull() {
    return ResponseEntity.badRequest().body(Map.of("message", "Event full"));
  }

}
