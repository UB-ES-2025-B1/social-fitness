package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  UserRepository userRepository;

  @Test
  void getUserById_returns200_whenFound() throws Exception {
    User user = new User("alice", "a@b.com", "p");
    user.setId(1L);
    user.setSports("[{\"sport\":\"tenis\",\"level\":\"INTERMEDIO\"}]");
    user.setBio("bio");
    user.setProfileImage("img");
    user.setCreatedAt(LocalDateTime.parse("2025-01-01T00:00:00"));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    mvc.perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.email").value("a@b.com"))
        .andExpect(jsonPath("$.sports").exists())
        .andExpect(jsonPath("$.profileImage").value("img"));
  }

  @Test
  void getUserById_returns404_whenMissing() throws Exception {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    mvc.perform(get("/users/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("User not found"));
  }
}

