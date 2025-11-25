package com.example.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import com.example.backend.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Autowired
    private Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        
        boolean isTestProfile = java.util.Arrays.asList(environment.getActiveProfiles()).contains("test");
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults()); // activar CORS
            
        // For test profile, permit all requests
        if (isTestProfile) {
            http
                .anonymous(anonymous -> anonymous.principal("testuser"))  // Allow anonymous access with test user
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
           http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                    .requestMatchers("/", "/auth/**").permitAll() 
                    .requestMatchers("/profile", "/profile/**").permitAll()
                    .requestMatchers("/sports/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll() // healthchecks allowed
                    .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/events").permitAll()
                    .requestMatchers(HttpMethod.GET, "/events/*").permitAll()
                    .requestMatchers("/uploads/**").permitAll()  
                    .requestMatchers("/events/*/chat/**").permitAll()   
                    .anyRequest().authenticated() 
                );
        }
        return http.build();
    }    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService, PasswordEncoder passwordEncoder
    ) {
        org.springframework.security.authentication.dao.DaoAuthenticationProvider authProvider = 
            new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}