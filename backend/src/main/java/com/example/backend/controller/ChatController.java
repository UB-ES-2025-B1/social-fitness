package com.example.backend.controller;

import com.example.backend.model.ChatMessage;
import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.EventRepository;
import com.example.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events/{id}/chat")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private AuthService authService;

    // 8) GET /events/:id/chat/messages
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        // Opcional: Validar si el evento existe
        if (!eventRepo.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("message", "Event not found"));
        }

         

        List<ChatMessage> messages = chatRepo.findByEventIdOrderByTimestampAsc(id);
        return ResponseEntity.ok(messages);
    }

    // 9) POST /events/:id/chat/messages
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", Map.of("text", "Message cannot be empty")));
        }

        try {
            // 1. Obtener usuario real autenticado
            User currentUser = authService.getCurrentAuthenticatedUser();

            // 2. Buscar el evento
            Event event = eventRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // 3. Validar que el usuario sea participante
             
            boolean isParticipant = event.getParticipantUsers().stream()
                    .anyMatch(u -> u.getId().equals(currentUser.getId()));

            if (!isParticipant) {
                return ResponseEntity.status(403).body(Map.of("message", "Not a participant"));
            }

            // 4. Guardar mensaje real
            ChatMessage message = new ChatMessage(
                id, 
                String.valueOf(currentUser.getId()), 
                currentUser.getUsername(), 
                text
            );
            chatRepo.save(message);

            return ResponseEntity.status(201).body(Map.of(
                "id", message.getId(),
                "message", "Message created"
            ));

        } catch (AuthService.ValidationException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        } catch (RuntimeException e) {
            if ("Event not found".equals(e.getMessage())) {
                return ResponseEntity.status(404).body(Map.of("message", "Event not found"));
            }
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Internal Server Error"));
        }
    }
}