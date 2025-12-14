package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.service.EventService;
import com.example.backend.service.AuthService;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventsController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventsControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @MockBean
  EventService service;

  @MockBean
  EventRepository repo;

  @MockBean
  UserRepository userRepository;

  @MockBean
  AuthService authService;

  @Test
  void join_returns200_withMessage() throws Exception {
    User mockUser = new User();
    mockUser.setId(1L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
    
    doNothing().when(service).join(any(Long.class), any(Long.class));
    mvc.perform(post("/events/1/join")
        .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Joined"));
  }

  @Test
  void leave_returns200_whenOk() throws Exception {
    User mockUser = new User();
    mockUser.setId(2L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
    
    doNothing().when(service).leave(any(Long.class), any(Long.class));
    mvc.perform(post("/events/2/leave")
        .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Left"));
  }

  @Test
  void leave_returns400_whenIllegalState() throws Exception {
    User mockUser = new User();
    mockUser.setId(3L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
    
    doThrow(new IllegalStateException("Not a participant")).when(service).leave(any(Long.class), any(Long.class));
    mvc.perform(post("/events/3/leave")
        .with(csrf()))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Not a participant"));
  }

  @Test
  void create_returns201_whenValid() throws Exception {
    User mockUser = new User();
    mockUser.setId(4L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);
    
    Event in = new Event();
    in.setTitle("Match");
    in.setSport("football");
    in.setDate(LocalDate.now().plusDays(2));
    in.setTime(LocalTime.of(18,0));
    in.setLocation("My Field");
    in.setOrganizer("Organizer");
    in.setCapacity(10);
    in.setPrice(BigDecimal.ZERO);

    Event saved = new Event();
    saved.setId(123L);
    saved.setTitle(in.getTitle());
    saved.setSport(in.getSport());
    saved.setDate(in.getDate());
    saved.setTime(in.getTime());
    saved.setLocation(in.getLocation());
    saved.setOrganizer(in.getOrganizer());
    saved.setCapacity(in.getCapacity());
    saved.setPrice(in.getPrice());

    when(service.create(any(Event.class), any(Long.class))).thenReturn(saved);
    String json = mapper.writeValueAsString(in);

    mvc.perform(post("/events")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(123))
      .andExpect(jsonPath("$.message").value("Event created"));
  }

  @Test
  void list_returns200_andDelegatesToService() throws Exception {
    when(service.search(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(Map.of("id", "1", "title", "Test")));

    mvc.perform(get("/events?q=test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("1"));
  }

  @Test
  void detail_returns404_whenEventNotFound() throws Exception {
    when(service.detail(999L)).thenThrow(new NoSuchElementException("Event not found"));

    mvc.perform(get("/events/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Event not found"));
  }

  @Test
  void join_returns400_whenEventFull() throws Exception {
    User mockUser = new User();
    mockUser.setId(1L);
    when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);

    doThrow(new EventService.EventFullException()).when(service).join(1L, 1L);

    mvc.perform(post("/events/1/join").with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Event full"));
  }

  @Test
  void join_returns401_whenNotAuthenticated() throws Exception {
    when(authService.getCurrentAuthenticatedUser())
        .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("no auth"));

    mvc.perform(post("/events/1/join").with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Authentication required"));
  }

  @Test
  void create_returns400_whenValidationException() throws Exception {
    when(service.create(any(Event.class), any()))
        .thenThrow(new EventService.ValidationException(Map.of("title", "Title is required")));

    String json = mapper.writeValueAsString(new Event());

    mvc.perform(post("/events")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.errors.title").value("Title is required"));
  }
}
