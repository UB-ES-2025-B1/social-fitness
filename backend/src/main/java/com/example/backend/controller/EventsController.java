// src/main/java/com/example/backend/controller/EventsController.java
package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import com.example.backend.service.EventService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/events")
public class EventsController {

  private final EventService service;
  private final com.example.backend.service.AuthService authService;

  public EventsController(EventService service, com.example.backend.service.AuthService authService/*, EventRepository repo*/) {
    this.service = service;
    this.authService = authService;

  }

  @GetMapping
  public ResponseEntity<?> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String sports,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String days,
      @RequestParam(required = false) String timeFrom,
      @RequestParam(required = false) String timeTo
  ) {
    return ResponseEntity.ok(service.search(q, sports, location, days, timeFrom, timeTo));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(service.detail(id));
    } catch (NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("message", "Event not found"));
    }
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<?> join(@PathVariable Long id) {
  try {
    // require authenticated user
    com.example.backend.model.User user = authService.getCurrentAuthenticatedUser();
    service.join(id, user.getId());
    return ResponseEntity.ok(Map.of("message", "Joined"));
  } catch (EventService.EventFullException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
  } catch (NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
  } catch (com.example.backend.service.AuthService.ValidationException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
  }
}

  @PostMapping("/{id}/leave")
  public ResponseEntity<?> leave(@PathVariable Long id) {
    try {
      com.example.backend.model.User user = authService.getCurrentAuthenticatedUser();
      service.leave(id, user.getId());
      return ResponseEntity.ok(Map.of("message", "Left"));
    } catch (IllegalStateException ex) {
      return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    } catch (com.example.backend.service.AuthService.ValidationException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
    }
  }

  // 👉 NUEVO ENDPOINT PARA CREAR EVENTOS
  @PostMapping
  public ResponseEntity<?> create(@RequestBody Event e) {
    try {
      Long userId = null;
      try {
        com.example.backend.model.User user = authService.getCurrentAuthenticatedUser();
        userId = user.getId();
      } catch (Exception ignore) { // not authenticated, proceed with provided organizer in payload
      }

      Event saved = service.create(e, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", saved.getId(), "message", "Event created"));
    } catch (EventService.ValidationException ex) {
      return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", ex.getErrors()));
    }
  }

}
