package com.example.backend.service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  UserRepository userRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  AuthenticationManager authenticationManager;

  @InjectMocks
  AuthService authService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void register_throwsValidationException_whenUsernameAlreadyTaken() {
    RegisterRequest req = new RegisterRequest();
    req.setUsername("taken");
    req.setEmail("a@b.com");
    req.setPassword("password123");

    when(userRepository.existsByUsername("taken")).thenReturn(true);

    AuthService.ValidationException ex = assertThrows(AuthService.ValidationException.class, () -> authService.register(req));
    assertEquals("Validation failed", ex.getMessage());
    assertEquals("Username already taken", ex.fieldErrors.get("username"));
    verify(userRepository, never()).save(any());
  }

  @Test
  void register_throwsValidationException_whenEmailAlreadyTaken() {
    RegisterRequest req = new RegisterRequest();
    req.setUsername("newuser");
    req.setEmail("taken@b.com");
    req.setPassword("password123");

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("taken@b.com")).thenReturn(true);

    AuthService.ValidationException ex = assertThrows(AuthService.ValidationException.class, () -> authService.register(req));
    assertEquals("Email already taken", ex.fieldErrors.get("email"));
    verify(userRepository, never()).save(any());
  }

  @Test
  void register_savesUser_andReturnsUserResponse_whenValid() {
    RegisterRequest req = new RegisterRequest();
    req.setUsername("newuser");
    req.setEmail("new@b.com");
    req.setPassword("password123");

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@b.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded");

    User saved = new User("newuser", "new@b.com", "encoded");
    saved.setId(10L);
    when(userRepository.save(any(User.class))).thenReturn(saved);

    UserResponse resp = authService.register(req);

    assertEquals("10", resp.getId());
    assertEquals("newuser", resp.getUsername());
    assertEquals("new@b.com", resp.getEmail());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void login_setsSecurityContext_andReturnsUserResponse_whenAuthenticationSucceeds() {
    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("password123");

    User principal = new User("alice", "alice@b.com", "encoded");
    principal.setId(1L);

    Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    when(authenticationManager.authenticate(any())).thenReturn(auth);

    UserResponse resp = authService.login(req);

    assertEquals("1", resp.getId());
    assertEquals("alice", resp.getUsername());
    assertEquals("alice@b.com", resp.getEmail());
    assertSame(auth, SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void login_throwsValidationException_whenBadCredentials() {
    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("wrong");

    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

    AuthService.ValidationException ex = assertThrows(AuthService.ValidationException.class, () -> authService.login(req));
    assertEquals("Invalid password", ex.fieldErrors.get("password"));
  }

  @Test
  void login_throwsValidationException_whenUsernameNotFound() {
    LoginRequest req = new LoginRequest();
    req.setUsername("missing");
    req.setPassword("password123");

    when(authenticationManager.authenticate(any())).thenThrow(new UsernameNotFoundException("User not found"));

    AuthService.ValidationException ex = assertThrows(AuthService.ValidationException.class, () -> authService.login(req));
    assertEquals("User not found", ex.fieldErrors.get("username"));
  }

  @Test
  void login_throwsValidationException_whenOtherAuthenticationError() {
    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("password123");

    when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationServiceException("boom"));

    AuthService.ValidationException ex = assertThrows(AuthService.ValidationException.class, () -> authService.login(req));
    assertEquals("Invalid credentials", ex.fieldErrors.get("general"));
  }

  @Test
  void getUserByUsername_throwsRuntimeException_whenNotFound() {
    when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.getUserByUsername("missing"));
    assertTrue(ex.getMessage().contains("User not found"));
  }

  @Test
  void getCurrentAuthenticatedUser_throws_whenNoAuthentication() {
    SecurityContextHolder.clearContext();
    assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentAuthenticatedUser());
  }

  @Test
  void getCurrentAuthenticatedUser_throws_whenAnonymousUser() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn("anonymousUser");

    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(authentication);
    SecurityContextHolder.setContext(ctx);

    assertThrows(UsernameNotFoundException.class, () -> authService.getCurrentAuthenticatedUser());
  }

  @Test
  void getCurrentAuthenticatedUser_returnsPrincipal_whenPrincipalIsUser() {
    User principal = new User("u", "u@b.com", "p");
    principal.setId(99L);

    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(principal);

    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(authentication);
    SecurityContextHolder.setContext(ctx);

    User result = authService.getCurrentAuthenticatedUser();
    assertSame(principal, result);
    verifyNoInteractions(userRepository);
  }

  @Test
  void getCurrentAuthenticatedUser_loadsFromRepository_whenPrincipalIsUserDetails() {
    UserDetails principal = org.springframework.security.core.userdetails.User
        .withUsername("alice")
        .password("x")
        .authorities("ROLE_USER")
        .build();

    User repoUser = new User("alice", "alice@b.com", "p");
    repoUser.setId(1L);
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(repoUser));

    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(principal);

    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(authentication);
    SecurityContextHolder.setContext(ctx);

    User result = authService.getCurrentAuthenticatedUser();
    assertSame(repoUser, result);
    verify(userRepository).findByUsername("alice");
  }
}

