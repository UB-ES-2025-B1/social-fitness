package com.example.backend.controller;

import com.example.backend.config.SecurityConfig;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationsController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationsControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    NotificationService notificationService;

    @MockBean
    AuthService authService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    AuthenticationManager authenticationManager;

    private User testUser;
    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");

        // Create Notification entity
        Notification testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(1L);
        testNotification.setType(Notification.NotificationType.NEW_MESSAGE);
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test message");
        testNotification.setRead(false);
        testNotification.setCreatedAt(Instant.now());

        // Create DTO from entity
        testNotificationDTO = new NotificationDTO(testNotification);

        // Mock AuthService to return testUser
        when(authService.getCurrentAuthenticatedUser()).thenReturn(testUser);

        // Mock user repository for authentication
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserNotifications_shouldReturn200() throws Exception {
        List<NotificationDTO> notifications = Arrays.asList(testNotificationDTO);
        when(notificationService.getNotifications(anyLong())).thenReturn(notifications);

        mvc.perform(get("/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].message").value("Test message"))
            .andExpect(jsonPath("$[0].read").value(false));

        verify(notificationService, times(1)).getNotifications(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsRead_shouldReturn200() throws Exception {
        doNothing().when(notificationService).markAsRead(anyLong(), anyLong());

        mvc.perform(put("/notifications/1/read")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAllAsRead_shouldReturn200() throws Exception {
        when(notificationService.markAllAsRead(anyLong())).thenReturn(5L);

        mvc.perform(put("/notifications/read-all")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(5));

        verify(notificationService, times(1)).markAllAsRead(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteNotification_shouldReturn200() throws Exception {
        doNothing().when(notificationService).deleteNotification(anyLong(), anyLong());

        mvc.perform(delete("/notifications/1")
                .with(csrf()))
            .andExpect(status().isOk());

        verify(notificationService, times(1)).deleteNotification(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUnreadCount_shouldReturn200() throws Exception {
        when(notificationService.getUnreadCount(anyLong())).thenReturn(3L);

        mvc.perform(get("/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3));

        verify(notificationService, times(1)).getUnreadCount(anyLong());
    }

    // ============================================================
    // ERROR CASES - Ajustados a la configuración real
    // ============================================================

    @Test
    @WithMockUser(username = "testuser")
    void markAsRead_shouldReturn403_whenNotAuthorized() throws Exception {
        doThrow(new IllegalAccessException("Not authorized"))
            .when(notificationService).markAsRead(eq(1L), anyLong());

        mvc.perform(put("/notifications/1/read")
                .with(csrf()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Not authorized to access this notification"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsRead_shouldReturn404_whenNotificationNotFound() throws Exception {
        doThrow(new NoSuchElementException("Notification not found"))
            .when(notificationService).markAsRead(eq(999L), anyLong());

        mvc.perform(put("/notifications/999/read")
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteNotification_shouldReturn403_whenNotAuthorized() throws Exception {
        doThrow(new IllegalAccessException("Not authorized"))
            .when(notificationService).deleteNotification(eq(1L), anyLong());

        mvc.perform(delete("/notifications/1")
                .with(csrf()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Not authorized to delete this notification"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteNotification_shouldReturn404_whenNotificationNotFound() throws Exception {
        doThrow(new NoSuchElementException("Notification not found"))
            .when(notificationService).deleteNotification(eq(999L), anyLong());

        mvc.perform(delete("/notifications/999")
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserNotifications_shouldReturnEmptyList_whenNoNotifications() throws Exception {
        when(notificationService.getNotifications(anyLong())).thenReturn(Arrays.asList());

        mvc.perform(get("/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUnreadCount_shouldReturn0_whenNoUnreadNotifications() throws Exception {
        when(notificationService.getUnreadCount(anyLong())).thenReturn(0L);

        mvc.perform(get("/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }
}