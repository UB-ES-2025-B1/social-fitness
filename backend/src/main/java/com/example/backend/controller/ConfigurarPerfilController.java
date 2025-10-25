package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ConfigurarPerfilController {
    
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    public ConfigurarPerfilController(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("/{userId}")
    public ResponseEntity<?> configurarPerfil(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        
        try {
            // Validar que sports existe y no está vacío
            List<?> sportsList = (List<?>) body.get("sports");
            if (sportsList == null || sportsList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Validation failed",
                    "errors", Map.of("sports", "Select at least one sport")
                ));
            }
            
            return userRepository.findById(userId)
                .map(user -> {
                    try {
                        String sportsJson = objectMapper.writeValueAsString(sportsList);
                        user.setSports(sportsJson);
                        
                        if (body.containsKey("bio")) {
                            user.setBio((String) body.get("bio"));
                        }
                        if (body.containsKey("profileImage")) {
                            user.setProfileImage((String) body.get("profileImage"));
                        }
                        
                        userRepository.save(user);
                        return ResponseEntity.ok(Map.of("message", "Profile saved"));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found")));
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}