package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigurarPerfilController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfigurarPerfilControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @MockBean
  UserRepository userRepository;

  @Test
  void whenSportsEmpty_shouldReturn400_withError() throws Exception {
    var body = Map.of("sports", new String[] {});
    String json = mapper.writeValueAsString(body);

    mvc.perform(post("/profile/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors.sports").exists());
  }

  @Test
  void whenSportsPresent_andUserExists_shouldSaveAndReturn200() throws Exception {
    User u = new User();
    u.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    var body = Map.of("sports", new String[] {"football"}, "bio", "hi");
    String json = mapper.writeValueAsString(body);

    mvc.perform(post("/profile/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Profile saved"));

    verify(userRepository).save(any(User.class));
  }

  @Test
  void whenUserNotFound_shouldReturn404() throws Exception {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    var body = Map.of("bio", "hello");
    String json = mapper.writeValueAsString(body);

    mvc.perform(post("/profile/999")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isNotFound());
  }
}
