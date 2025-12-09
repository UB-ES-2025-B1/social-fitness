// src/main/java/com/example/backend/controller/EventsController.java
package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import com.example.backend.service.EventService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/events")
public class EventsController {

  private final EventService service;
  private final com.example.backend.service.AuthService authService;
  private final UserRepository userRepo;

  public EventsController(EventService service, com.example.backend.service.AuthService authService, UserRepository userRepo) {
    this.service = service;
    this.authService = authService;
    this.userRepo = userRepo;
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
      // 1. Obtener el detalle completo del servicio (mantiene toda la lógica original)
      Map<String, Object> eventDetail = service.detail(id);
      
      // 2. Solo sobrescribir 'participants' para añadir profileImage
      Event event = service.findById(id);
      if (event != null) {
        List<User> users = userRepo.findParticipantsByEventId(event.getId());
        List<Map<String, Object>> participants = users.stream().map(u -> {
          Map<String, Object> p = new HashMap<>();
          p.put("id", u.getId());
          p.put("name", u.getUsername());
          p.put("profileImage", u.getProfileImage());
          return p;
        }).toList();
        eventDetail.put("participants", participants);
      }
      
      return ResponseEntity.ok(eventDetail);
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Evento no encontrado"));
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

  // Lista completa de participantes del evento
  @GetMapping("/{id}/participants")
  public ResponseEntity<?> getParticipants(@PathVariable Long id) {
    Event event = service.findById(id);
    if (event == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Event not found"));
    }

    // Usa la relación ya cargada: getParticipantUsers()
    List<Map<String, Object>> participants = event.getParticipantUsers().stream()
      .map(u -> {
        Map<String, Object> p = new HashMap<>();
        p.put("id", u.getId());
        p.put("name", u.getUsername());
        p.put("profileImage", u.getProfileImage()); // puede ser null
        return p;
      })
      .toList();

    return ResponseEntity.ok(participants);
  }

  @GetMapping("/{id}/participants/{userId}")
  public ResponseEntity<?> getParticipant(@PathVariable Long id, @PathVariable Long userId) {
    Event event = service.findById(id);
    if (event == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Event not found"));
    }

    // Chequea pertenencia usando la lista participantUsers del evento
    com.example.backend.model.User match = event.getParticipantUsers().stream()
      .filter(u -> Objects.equals(u.getId(), userId))
      .findFirst()
      .orElse(null);

    if (match == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Participant not in event"));
    }

    Map<String, Object> p = new HashMap<>();
    p.put("id", match.getId());
    p.put("name", match.getUsername());
    p.put("profileImage", match.getProfileImage());
    return ResponseEntity.ok(p);
  }
}