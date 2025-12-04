package com.example.backend.controller;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.service.AuthService;
import com.example.backend.service.DirectMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/messages")
public class DirectMessagesController {

    private final DirectMessageService service;
    private final AuthService auth;

    public DirectMessagesController(DirectMessageService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
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
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats() {
        try {
            User me = auth.getCurrentAuthenticatedUser();

            // obtener todos los mensajes donde yo soy sender o receiver
            List<DirectMessage> msgs = service.getAllMessagesOfUser(me.getId());

            // agrupar por otro usuario
            Map<Long, List<DirectMessage>> grouped = new HashMap<>();

            for (DirectMessage m : msgs) {
                Long other = m.getSender().getId().equals(me.getId())
                        ? m.getReceiver().getId()
                        : m.getSender().getId();

                grouped.computeIfAbsent(other, k -> new ArrayList<>()).add(m);
            }

            List<Map<String, Object>> result = new ArrayList<>();

            for (var entry : grouped.entrySet()) {
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
                                "profileImage", other.getProfileImage()
                        ),
                        "lastMessage", Map.of(
                                "text", last.getText(),
                                "timestamp", last.getTimestamp().toString(),
                                "senderId", last.getSender().getId()
                        ),
                        "unreadCount", unread
                ));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        }
    }


}
