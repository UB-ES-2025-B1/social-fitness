package com.example.backend;

import com.example.backend.controller.AuthController;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
import com.example.backend.config.SecurityConfig;
import com.example.backend.repository.UserRepository;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  AuthService authService;

  @MockBean
  UserRepository userRepository;
  
  @Test
  void register_shouldReturn201_whenPayloadValid() throws Exception {
    // stub the service to avoid DB dependencies
    UserResponse resp = new UserResponse("1", "qa_user", "qa_user@mail.com");
    when(authService.register(any())).thenReturn(resp);

    String json = """
    {
      "username": "qa_user_123",
      "email": "qa_user_123@mail.com",
      "password": "123456789abc"
    }
    """;

    mvc.perform(post("/auth/register")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.user.username").value("qa_user"));
  }

  @Test
  void register_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "email":"invalid_without_username_and_pwd" }
    """;
    mvc.perform(post("/auth/register")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isBadRequest()); // @Valid dispara 400
  }

  @Test
  void login_shouldReturn200_whenValidCredentialsFormat() throws Exception {
    UserResponse resp = new UserResponse("1", "qa_user_5", "qa_user_5@mail.com");
    when(authService.login(any())).thenReturn(resp);

    var json = """
      { "username":"qa_user_5", "password":"123456789abc" }
    """;
    mvc.perform(post("/auth/login")
        .with(csrf())
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
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isBadRequest()); // por @Valid
  }
}
