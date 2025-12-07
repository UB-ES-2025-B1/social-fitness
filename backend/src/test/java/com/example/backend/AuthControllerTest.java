package com.example.backend;

import com.example.backend.controller.AuthController;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;   
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationManager;

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
@AutoConfigureMockMvc 
@ActiveProfiles("test")   

class AuthControllerTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  AuthService authService;

  @MockBean
  UserRepository userRepository;
  
  @MockBean
  AuthenticationManager authenticationManager;

  @Test
  @WithMockUser   
  void register_shouldReturn201_whenPayloadValid() throws Exception {
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
  @WithMockUser
  void register_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "email":"invalid_without_username_and_pwd" }
    """;
    mvc.perform(post("/auth/register")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void login_shouldReturn200_whenValidCredentialsFormat() throws Exception {
    //  CREAR UN USER MOCK
    User mockUser = new User("qa_user_5", "qa_user_5@mail.com", "encoded_password");
    mockUser.setId(1L);
    
    //  CREAR AUTHENTICATION MOCK
    Authentication mockAuth = new UsernamePasswordAuthenticationToken(
        mockUser, 
        null, 
        mockUser.getAuthorities()
    );
    
    //  MOCKEAR authenticationManager.authenticate()
    when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
    
    //  MOCKEAR authService.login()
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
  @WithMockUser
  void login_shouldReturn400_whenMissingFields() throws Exception {
    var json = """
      { "username":"qa_user" }
    """;
    mvc.perform(post("/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }
}