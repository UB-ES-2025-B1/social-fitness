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
            // Validar sports SOLO si está presente en el body
            if (body.containsKey("sports")) {
                List<?> sportsList = (List<?>) body.get("sports");
                
                // Si sports está presente pero es un array vacío, devolver error
                if (sportsList == null || sportsList.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "Validation failed",
                        "errors", Map.of("sports", "Select at least one sport")
                    ));
                }
                
                // Si sports tiene elementos, validarlo y guardarlo
                return userRepository.findById(userId)
                    .map(user -> {
                        try {
                            String sportsJson = objectMapper.writeValueAsString(sportsList);
                            user.setSports(sportsJson);
                            
                            // Actualizar bio y profileImage si están presentes
                            if (body.containsKey("bio")) {
                                user.setBio((String) body.get("bio"));
                            }
                            if (body.containsKey("profileImage")) {
                                user.setProfileImage((String) body.get("profileImage"));
                            }
                            
                            userRepository.save(user);
                            return ResponseEntity.ok(Map.of("message", "Profile saved"));
                        } catch (Exception e) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "Error saving profile"));
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
            } else {
                // Si sports NO está en el body, solo actualizar bio/profileImage
                return userRepository.findById(userId)
                    .map(user -> {
                        boolean updated = false;
                        
                        if (body.containsKey("bio")) {
                            user.setBio((String) body.get("bio"));
                            updated = true;
                        }
                        if (body.containsKey("profileImage")) {
                            user.setProfileImage((String) body.get("profileImage"));
                            updated = true;
                        }
                        
                        if (updated) {
                            userRepository.save(user);
                            return ResponseEntity.ok(Map.of("message", "Profile saved"));
                        } else {
                            return ResponseEntity.badRequest().body(Map.of(
                                "message", "No fields to update"
                            ));
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error processing request"));
        }
    }
}