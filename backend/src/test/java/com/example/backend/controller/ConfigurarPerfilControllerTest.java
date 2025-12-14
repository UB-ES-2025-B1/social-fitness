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
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

  @Test
  void whenNoSports_andBioPresent_shouldSaveAndReturn200() throws Exception {
    User u = new User();
    u.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    var body = Map.of("bio", "updated");
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
  void whenNoFieldsProvided_shouldReturn400() throws Exception {
    User u = new User();
    u.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    String json = mapper.writeValueAsString(Map.of());

    mvc.perform(post("/profile/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("No fields to update"));
  }

  @Test
  void uploadAvatar_shouldReturn400_whenFileEmpty() throws Exception {
    MockMultipartFile file = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[0]);

    mvc.perform(multipart("/profile/1/avatar")
        .file(file)
        .with(csrf()))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("File is empty"));
  }

  @Test
  void uploadAvatar_shouldReturn404_whenUserNotFound() throws Exception {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    MockMultipartFile file = new MockMultipartFile("avatar", "avatar.png", "image/png", "x".getBytes());

    mvc.perform(multipart("/profile/1/avatar")
        .file(file)
        .with(csrf()))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  void uploadAvatar_shouldReturn200_whenUploadOk() throws Exception {
    User u = new User();
    u.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenReturn(u);

    MockMultipartFile file = new MockMultipartFile("avatar", "avatar.png", "image/png", "hello".getBytes());

    mvc.perform(multipart("/profile/1/avatar")
        .file(file)
        .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.profileImage").value(org.hamcrest.Matchers.containsString("/uploads/avatars/")));
  }
}
