package com.example.backend.controller;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.repository.DirectMessageRepository;  
import com.example.backend.service.AuthService;
import com.example.backend.service.DirectMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.repository.UserRepository;

import java.util.*;

@RestController
@RequestMapping("/messages")
public class DirectMessagesController {

    private final DirectMessageService service;
    private final AuthService auth;
    private final UserRepository userRepository;
    private final DirectMessageRepository messageRepository; 


    public DirectMessagesController(DirectMessageService service, AuthService auth, UserRepository userRepository,DirectMessageRepository messageRepository) {
        this.service = service;
        this.auth = auth;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository; 

    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getMessages(@PathVariable Long userId) {
        try {
            User me = auth.getCurrentAuthenticatedUser();
            List<DirectMessage> msgs = service.getConversation(me.getId(), userId);

            // mark as read
            service.markConversationRead(me.getId(), userId);

            List<Map<String, Object>> result = msgs.stream().map(m -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", m.getId());
                map.put("senderId", m.getSender().getId());
                map.put("senderUsername", m.getSender().getUsername());
                map.put("receiverId", m.getReceiver().getId());
                map.put("text", m.getText());
                map.put("timestamp", m.getTimestamp().toString());
                map.put("read", m.isRead());
                return map;
            }).toList();

            return ResponseEntity.ok(result);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        } catch (Exception e) {
            System.err.println("🔴 [GET /messages/users/" + userId + "] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        try {
            User sender = auth.getCurrentAuthenticatedUser();
            String text = body.get("text");

            if (text == null || text.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Validation failed",
                        "errors", Map.of("text", "Message cannot be empty")
                ));
            }

            DirectMessage msg = service.sendMessage(sender.getId(), userId, text.trim());

            return ResponseEntity.status(201).body(Map.of(
                    "id", msg.getId(),
                    "senderId", msg.getSender().getId(),
                    "senderUsername", msg.getSender().getUsername(),
                    "receiverId", msg.getReceiver().getId(),
                    "text", msg.getText(),
                    "timestamp", msg.getTimestamp().toString(),
                    "read", false,
                    "message", "Message sent"
            ));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        } catch (Exception e) {
            System.err.println("🔴 [POST /messages/users/" + userId + "] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats() {
        try {
            System.out.println("🔵 [GET /chats] Starting...");
            
            User me = auth.getCurrentAuthenticatedUser();
            System.out.println("🔵 [GET /chats] User: " + me.getUsername() + " (ID: " + me.getId() + ")");

            // obtener todos los mensajes donde yo soy sender o receiver
            List<DirectMessage> msgs = service.getAllMessagesOfUser(me.getId());
            System.out.println("🔵 [GET /chats] Found " + msgs.size() + " messages");

            // agrupar por otro usuario
            Map<Long, List<DirectMessage>> grouped = new HashMap<>();

            for (DirectMessage m : msgs) {
                Long other = m.getSender().getId().equals(me.getId())
                        ? m.getReceiver().getId()
                        : m.getSender().getId();

                grouped.computeIfAbsent(other, k -> new ArrayList<>()).add(m);
            }
            
            System.out.println("🔵 [GET /chats] Grouped into " + grouped.size() + " conversations");

            List<Map<String, Object>> result = new ArrayList<>();

            for (var entry : grouped.entrySet()) {
                try {
                    List<DirectMessage> conv = entry.getValue();
                    conv.sort(Comparator.comparing(DirectMessage::getTimestamp).reversed());

                    DirectMessage last = conv.get(0);
                    long unread = conv.stream()
                            .filter(m -> !m.isRead() && m.getReceiver().getId().equals(me.getId()))
                            .count();

                    User other = last.getSender().getId().equals(me.getId())
                            ? last.getReceiver()
                            : last.getSender();

                    result.add(Map.of(
                            "id", "chat-" + me.getId() + "-" + other.getId(),
                            "otherUser", Map.of(
                                    "id", other.getId(),
                                    "username", other.getUsername(),
                                    "profileImage", other.getProfileImage() != null ? other.getProfileImage() : "" 
                            ),
                            "lastMessage", Map.of(
                                    "text", last.getText(),
                                    "timestamp", last.getTimestamp().toString(),
                                    "senderId", last.getSender().getId()
                            ),
                            "unreadCount", unread
                    ));
                } catch (Exception ex) {
                    System.err.println("🔴 [GET /chats] Error processing conversation with user " + entry.getKey() + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
            System.out.println("🔵 [GET /chats] Returning " + result.size() + " chats");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("🔴 [GET /chats] ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        try {
            System.out.println("🔵 [GET /users/search] Query: " + q);
            
            User me = auth.getCurrentAuthenticatedUser();
            System.out.println("🔵 [GET /users/search] User: " + me.getUsername());

            if (q == null || q.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Query parameter 'q' is required"));
            }

            List<User> results = userRepository.findByUsernameContainingIgnoreCase(q);
            System.out.println("🔵 [GET /users/search] Found " + results.size() + " users");

            return ResponseEntity.ok(
                    results.stream()
                            .filter(u -> !u.getId().equals(me.getId()))
                            .map(u -> Map.of(
                                    "id", u.getId(),
                                    "username", u.getUsername(),
                                    "profileImage", u.getProfileImage() != null ? u.getProfileImage() : "" // ✅ NULL-SAFE
                            ))
                            .toList()
            );

        } catch (Exception e) {
            System.err.println("🔴 [GET /users/search] ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        try {
            User me = auth.getCurrentAuthenticatedUser();
            Optional<DirectMessage> msgOpt = messageRepository.findById(id);
            
            if (msgOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Message not found"));
            }

            DirectMessage msg = msgOpt.get();
            
            // Verificar que soy el receptor
            if (!msg.getReceiver().getId().equals(me.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }

            msg.setRead(true);
            messageRepository.save(msg);

            return ResponseEntity.ok(Map.of("message", "Message marked as read"));

        } catch (Exception e) {
            System.err.println("🔴 [PUT /messages/" + id + "/read] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }

     
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable String id) {
        try {
            User me = auth.getCurrentAuthenticatedUser();
            Optional<DirectMessage> msgOpt = messageRepository.findById(id);
            
            if (msgOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Message not found"));
            }

            DirectMessage msg = msgOpt.get();
            
            // Verificar que soy el remitente
            if (!msg.getSender().getId().equals(me.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }

            messageRepository.delete(msg);

            return ResponseEntity.ok(Map.of("message", "Message deleted"));

        } catch (Exception e) {
            System.err.println("🔴 [DELETE /messages/" + id + "] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }
}