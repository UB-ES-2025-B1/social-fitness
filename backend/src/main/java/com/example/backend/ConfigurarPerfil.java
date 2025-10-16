package com.example.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/profile")
public class ConfigurarPerfil {

    @PostMapping
    public ResponseEntity<?> configurarPerfil(@RequestBody Map<String, List<Map<String, String>>> body) {
         
        List<Map<String, String>> sportsList = body.get("sports");
        if (sportsList == null || sportsList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Validation failed",
                "errors", Map.of("sports", "Select at least one sport")
            ));
        }

        // Aquí iría la lógica para guardar el perfil en la base de datos, vinculado al usuario loggeado

        return ResponseEntity.ok(Map.of("message", "Profile saved"));
    }
}