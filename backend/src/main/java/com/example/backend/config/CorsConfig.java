package com.example.backend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // Orígenes exactos (con puerto). No usar "*" si allowCredentials=true
    cfg.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    ));
    cfg.setAllowCredentials(true);

    // Métodos y cabeceras que aceptamos en el preflight
    cfg.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(Arrays.asList("*")); // acepta Content-Type, Authorization, etc.
    cfg.setExposedHeaders(Arrays.asList("Location", "Set-Cookie"));
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
