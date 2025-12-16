package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.EventRepository;
import com.example.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerWebMvcTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  ChatMessageRepository chatRepo;

  @MockBean
  EventRepository eventRepo;

  @MockBean
  AuthService authService;

  @Test
  void getMessages_returns404_whenEventDoesNotExist() throws Exception {
    when(eventRepo.existsById(1L)).thenReturn(false);

    mvc.perform(get("/events/1/chat/messages"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Event not found"));
  }

  @Test
  void sendMessage_returns400_whenTextEmpty() throws Exception {
    mvc.perform(post("/events/1/chat/messages")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.text").value("Message cannot be empty"));
  }

  @Test
  void sendMessage_returns401_whenAuthenticationRequired() throws Exception {
    when(authService.getCurrentAuthenticatedUser()).thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("no"));

    mvc.perform(post("/events/1/chat/messages")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":\"hello\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  void sendMessage_returns404_whenEventMissing() throws Exception {
    User user = new User("alice", "a@b.com", "p");
    user.setId(1L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(user);
    when(eventRepo.findById(1L)).thenReturn(Optional.empty());

    mvc.perform(post("/events/1/chat/messages")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":\"hello\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Event not found"));
  }

  @Test
  void sendMessage_returns403_whenUserNotParticipant() throws Exception {
    User user = new User("alice", "a@b.com", "p");
    user.setId(1L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(user);

    Event event = new Event();
    event.setId(1L);
    event.setTitle("Test");
    event.setSport("Tennis");
    event.setLocation("Court");
    event.setDate(LocalDate.now().plusDays(1));
    event.setTime(LocalTime.of(10, 0));
    event.setOrganizer("org");
    event.setCapacity(10);
    event.setPrice(BigDecimal.ZERO);

    when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

    mvc.perform(post("/events/1/chat/messages")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":\"hello\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Not a participant"));
  }
}

