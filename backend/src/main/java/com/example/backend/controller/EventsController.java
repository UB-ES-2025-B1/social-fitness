// src/main/java/com/example/backend/controller/EventsController.java
package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import com.example.backend.service.EventService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventsController {

  private final EventService service;
  private final EventRepository repo;

  public EventsController(EventService service, EventRepository repo) {
    this.service = service;
    this.repo = repo;
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
    return ResponseEntity.ok(service.detail(id));
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<?> join(@PathVariable Long id) {
    service.join(id);
    return ResponseEntity.ok(Map.of("message", "Joined"));
  }

  // 👉 NUEVO ENDPOINT PARA CREAR EVENTOS
  @PostMapping
  public ResponseEntity<?> create(@RequestBody Event e) {
    Event saved = repo.save(e);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("id", saved.getId(), "message", "Event created"));
  }
}
