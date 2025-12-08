package com.example.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;
  
  @Autowired
  private AuthenticationManager authenticationManager;
  
  @Autowired
  private SecurityContextRepository securityContextRepository;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {
    UserResponse user = authService.register(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("user", user));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @Valid @RequestBody LoginRequest body, 
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    try {
      System.out.println("🔵 [LOGIN] Starting login for: " + body.getUsername());
      
      //  Autenticar con Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              body.getUsername(),
              body.getPassword()
          )
      );
      
      System.out.println("🔵 [LOGIN] Authentication successful: " + authentication.getName());

      //  Crear contexto de seguridad
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      
      System.out.println("🔵 [LOGIN] SecurityContext created");

      //   Crear/obtener sesión HTTP
      HttpSession session = request.getSession(true);
      System.out.println("🔵 [LOGIN] Session ID: " + session.getId());
      
      //  Guardar contexto en la sesión MANUALMENTE (doble seguridad)
      session.setAttribute("SPRING_SECURITY_CONTEXT", context);
      System.out.println("🔵 [LOGIN] Context saved in session manually");
      
      Object savedContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
      System.out.println("🔵 [LOGIN] Context retrieved from session: " + (savedContext != null ? "YES" : "NO"));
      if (savedContext != null) {
          SecurityContext ctx = (SecurityContext) savedContext;
          System.out.println("🔵 [LOGIN] Context auth in session: " + (ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "NULL"));
      }
      //  Guardar usando el repository también
      securityContextRepository.saveContext(context, request, response);
      System.out.println("🔵 [LOGIN] Context saved via repository");

      // Obtener datos del usuario
      UserResponse user = authService.login(body);
      
      System.out.println("🔵 [LOGIN] Login complete for user: " + user.getUsername());

      return ResponseEntity.ok(Map.of(
          "user", user, 
          "message", "Login successful",
          "sessionId", session.getId() // Para debug
      ));
      
    } catch (Exception e) {
      System.err.println("🔴 [LOGIN] ERROR: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Invalid credentials"));
    }
  }
 
}