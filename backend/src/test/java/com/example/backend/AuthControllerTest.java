package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired MockMvc mvc;

  @Test
  void register_shouldReturn201_whenPayloadValid() throws Exception {
    String suffix = String.valueOf(System.nanoTime()); // o UUID.randomUUID().toString()
    String json = """
    {
      "username": "qa_user_4",
      "email": "qa_user_%s@mail.com",
      "password": "123456789abc"
    }
    """.formatted(suffix, suffix);
    mvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
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
      .andDo(print())
      .andExpect(status().isBadRequest()); // @Valid dispara 400
  }

  @Test
  void login_shouldReturn200_whenValidCredentialsFormat() throws Exception {
    var json = """
      { "username":"qa_user_4", "password":"123456789abc" }
    """;
    mvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void login_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "username":"qa_user" }
    """;
    mvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isBadRequest()); // por @Valid
  }
}
