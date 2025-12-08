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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.backend.repository.UserRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
public class SecurityConfig {

    @Autowired
    private Environment environment;

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

        @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository scr) throws Exception {
        boolean isTestProfile = java.util.Arrays.asList(environment.getActiveProfiles()).contains("test");
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .securityContext(context -> context.securityContextRepository(scr));
        
        
        if (!isTestProfile) {
            http.addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
                        throws java.io.IOException, jakarta.servlet.ServletException {
                    
                    HttpSession session = request.getSession(false);
                    String sessionId = session != null ? session.getId() : "NO SESSION";
                    
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String authInfo = auth != null ? auth.getName() + " (" + auth.isAuthenticated() + ")" : "NULL";
                    
                    System.out.println("🟢 [FILTER] " + request.getMethod() + " " + request.getRequestURI() + 
                                     " | Session: " + sessionId + " | Auth: " + authInfo);
                    
                    // Ver si la sesión tiene el contexto guardado
                    if (session != null) {
                        Object ctx = session.getAttribute("SPRING_SECURITY_CONTEXT");
                        System.out.println("🟢 [FILTER] Context in session: " + (ctx != null ? "YES" : "NO"));
                    }
                    
                    filterChain.doFilter(request, response);
                }
            }, org.springframework.security.web.context.SecurityContextHolderFilter.class); 
        }
            
        if (isTestProfile) {
            http
                .anonymous(anonymous -> anonymous.principal("testuser"))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/", "/auth/**").permitAll() 
                    .requestMatchers("/profile", "/profile/**").permitAll()
                    .requestMatchers("/sports/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/events").permitAll()
                    .requestMatchers(HttpMethod.GET, "/events/*").permitAll()
                    .requestMatchers("/events/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()  
                    .requestMatchers("/events/*/chat/**").permitAll()   
                    .requestMatchers("/messages/ws/","/notifications/ws").permitAll() // WebSocket will check auth internally
                    .anyRequest().authenticated() 
                )
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );
        }
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    }   
    @Bean
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
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
