package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Value("${ALLOWED_ORIGINS:http://localhost:5173,https://social-fitness-develop.netlify.app,https://social-fitness-preprod.netlify.app,https://social-fitness.netlify.app}") 
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println(">>> CORS - Allowed origins: " + allowedOrigins);
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        //   LIMPIAR ESPACIOS Y BARRAS FINALES , esto es por sí alguien pone otra url y sale un 403
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .map(origin -> origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin)
            .collect(Collectors.toList());
        
        configuration.setAllowedOrigins(origins);
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        configuration.setAllowCredentials(true);
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Set-Cookie"
        ));
        
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}