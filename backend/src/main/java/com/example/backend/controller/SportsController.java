package com.example.backend.controller;

import com.example.backend.model.UserSport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sports")
@CrossOrigin(origins = "http://localhost:5173")
public class SportsController {
    
    private static final List<String> AVAILABLE_SPORTS = Arrays.asList(
        "futbol",
        "basquet",
        "tenis",
        "natacio",
        "ciclisme",
        "correr",
        "gimnasia",
        "senderisme",
        "padel",
        "surf"
    );
    
    @GetMapping
    public ResponseEntity<?> getSports() {
        Map<String, Object> response = new HashMap<>();
        response.put("sports", AVAILABLE_SPORTS);
        response.put("levels", getSportLevelsList());
        response.put("total_sports", AVAILABLE_SPORTS.size());
        response.put("total_levels", getSportLevelsList().size());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/levels")
    public ResponseEntity<?> getSportLevels() {
        Map<String, Object> response = new HashMap<>();
        response.put("levels", getSportLevelsList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSports() {
        Map<String, Object> response = new HashMap<>();
        response.put("sports", AVAILABLE_SPORTS);
        return ResponseEntity.ok(response);
    }
    
    private List<String> getSportLevelsList() {
        return Arrays.stream(UserSport.Level.values())
            .map(Enum::name)
            .collect(Collectors.toList());
    }
}