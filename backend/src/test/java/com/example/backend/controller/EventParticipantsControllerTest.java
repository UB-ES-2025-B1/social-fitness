package com.example.backend.controller;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventParticipantsController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventParticipantsControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  EventRepository eventRepository;

  @Test
  void listParticipants_returns200_withCountAndCapacity() throws Exception {
    Event event = new Event();
    event.setId(10L);
    event.setTitle("Test");
    event.setSport("Tennis");
    event.setLocation("Court");
    event.setDate(LocalDate.now().plusDays(1));
    event.setTime(LocalTime.of(10, 0));
    event.setOrganizer("organizer");
    event.setCapacity(5);
    event.setPrice(BigDecimal.ZERO);

    User u1 = new User("alice", "a@b.com", "p");
    u1.setId(1L);
    u1.setProfileImage("img1");
    User u2 = new User("bob", "b@b.com", "p");
    u2.setId(2L);
    u2.setProfileImage("img2");
    event.addParticipant(u1);
    event.addParticipant(u2);

    when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

    mvc.perform(get("/events/10/participants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(2))
        .andExpect(jsonPath("$.capacity").value(5))
        .andExpect(jsonPath("$.participants[*].id", Matchers.containsInAnyOrder(1, 2)));
  }

  @Test
  void listParticipants_returns404_whenEventMissing() throws Exception {
    when(eventRepository.findById(999L)).thenReturn(Optional.empty());

    mvc.perform(get("/events/999/participants"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Event not found"));
  }

  @Test
  void isParticipant_returnsTrue_whenUserIsParticipant() throws Exception {
    Event event = new Event();
    event.setId(10L);
    event.setTitle("Test");
    event.setSport("Tennis");
    event.setLocation("Court");
    event.setDate(LocalDate.now().plusDays(1));
    event.setTime(LocalTime.of(10, 0));
    event.setOrganizer("organizer");
    event.setCapacity(5);
    event.setPrice(BigDecimal.ZERO);

    User u1 = new User("alice", "a@b.com", "p");
    u1.setId(1L);
    event.addParticipant(u1);

    when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

    mvc.perform(get("/events/10/participants/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isParticipant").value(true))
        .andExpect(jsonPath("$.capacity").value(5))
        .andExpect(jsonPath("$.participants").value(1));
  }

  @Test
  void isParticipant_returnsFalse_whenUserIsNotParticipant() throws Exception {
    Event event = new Event();
    event.setId(10L);
    event.setTitle("Test");
    event.setSport("Tennis");
    event.setLocation("Court");
    event.setDate(LocalDate.now().plusDays(1));
    event.setTime(LocalTime.of(10, 0));
    event.setOrganizer("organizer");
    event.setCapacity(5);
    event.setPrice(BigDecimal.ZERO);

    User u1 = new User("alice", "a@b.com", "p");
    u1.setId(1L);
    event.addParticipant(u1);

    when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

    mvc.perform(get("/events/10/participants/999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isParticipant").value(false))
        .andExpect(jsonPath("$.capacity").value(5))
        .andExpect(jsonPath("$.participants").value(1));
  }
}

