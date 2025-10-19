package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired MockMvc mvc;

  @Test
  void register_shouldReturn201_whenPayloadValid() throws Exception {
    var json = """
      { "email":"qa_user@mail.com",
        "username":"qa_user",
        "password":"12345678" }
    """;
    mvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isCreated()); // 201
  }

  @Test
  void register_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "email":"invalid_without_username_and_pwd" }
    """;
    mvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest()); // @Valid dispara 400
  }

  @Test
  void login_shouldReturn200_whenValidCredentialsFormat() throws Exception {
    var json = """
      { "email":"qa_user@mail.com", "password":"12345678" }
    """;
    mvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void login_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "email":"qa_user@mail.com" }
    """;
    mvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest()); // por @Valid
  }
}
