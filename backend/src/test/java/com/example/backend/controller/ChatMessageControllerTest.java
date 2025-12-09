package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testEventId;
    private User testUser;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        Event event = new Event();
        event.setTitle("Test Event");
        event.setSport("Football");
        event.setLocation("Stadium");
        event.setDate(LocalDate.now().plusDays(5));
        event.setTime(LocalTime.of(18, 0));
        event.setOrganizer(testUser.getUsername());
        event.setCapacity(20);
        event.setPrice(BigDecimal.ZERO);
        
        event.addParticipant(testUser);
        
        event = eventRepository.save(event);
        this.testEventId = event.getId();
    }

    @Test
    @WithMockUser(username = "testuser")
    void sendMessage_shouldReturn201() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("text", "Great event!");

        //    El endpoint devuelve {"message":"Message created","id":"..."}
        mockMvc.perform(post("/events/" + testEventId + "/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Message created"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getMessages_shouldReturn200() throws Exception {
        //   Crear mensaje
        Map<String, String> request = new HashMap<>();
        request.put("text", "Hello!");

        mockMvc.perform(post("/events/" + testEventId + "/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());

        //   Obtener mensajes
        mockMvc.perform(get("/events/" + testEventId + "/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].text").value("Hello!"))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

     
}