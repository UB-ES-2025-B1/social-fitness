package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events/{eventId}/participants")
public class EventParticipantsController {

    private final EventRepository eventRepository;

    public EventParticipantsController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Returns the list of participants for an event with their username and profile image.
     * Also returns capacity/count so the UI can show "x/y participantes".
     */
    @GetMapping
    public ResponseEntity<?> listParticipants(@PathVariable Long eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NoSuchElementException("Event not found"));

            List<Map<String, Object>> participants = event.getParticipantUsers().stream()
                    .map(user -> {
                        Map<String, Object> dto = new LinkedHashMap<>();
                        dto.put("id", user.getId());
                        dto.put("username", user.getUsername());
                        dto.put("profileImage", user.getProfileImage());
                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("participants", participants);
            payload.put("count", participants.size());
            payload.put("capacity", event.getCapacity());

            return ResponseEntity.ok(payload);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", "Event not found"));
        }
    }

    /**
     * Simple helper endpoint used by the frontend to check if a user is part of the event.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> isParticipant(@PathVariable Long eventId, @PathVariable Long userId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NoSuchElementException("Event not found"));

            boolean isParticipant = event.getParticipantUsers().stream()
                    .anyMatch(u -> u.getId().equals(userId));

            return ResponseEntity.ok(Map.of(
                    "isParticipant", isParticipant,
                    "capacity", event.getCapacity(),
                    "participants", event.getParticipants()
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", "Event not found"));
        }
    }
}
