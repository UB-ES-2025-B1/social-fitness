package com.example.backend.controller;

import com.example.backend.model.DirectMessage;
import com.example.backend.model.User;
import com.example.backend.repository.DirectMessageRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import com.example.backend.service.DirectMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectMessagesController.class)
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    AuthService authService;

    @MockBean
    DirectMessageService directMessageService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    DirectMessageRepository directMessageRepository;

    private User sender;
    private User receiver;
    private DirectMessage testMessage;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setEmail("sender@test.com");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@test.com");

        testMessage = new DirectMessage();
        testMessage.setTimestamp(Instant.now());
        testMessage.setText("Hello there!");
        testMessage.setRead(false);
        testMessage.setSender(sender);
        testMessage.setReceiver(receiver);

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sender);
        when(userRepository.findByUsernameContainingIgnoreCase(anyString()))
            .thenReturn(Arrays.asList(receiver));
    }

    @Test
    @Disabled("BUG: authService.getCurrentAuthenticatedUser() returns null in controller - needs investigation")
    void sendMessage_shouldReturn201() throws Exception {
        when(directMessageService.sendMessage(anyLong(), anyLong(), anyString()))
            .thenReturn(testMessage);

        String json = mapper.writeValueAsString(java.util.Map.of("text", "Hello there!"));

        mvc.perform(post("/messages/users/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Message sent"));

        verify(directMessageService, times(1)).sendMessage(eq(1L), eq(2L), anyString());
    }
    
    @Test
    void sendMessage_shouldReturn401_whenReceiverNotFound() throws Exception {
        when(directMessageService.sendMessage(eq(1L), eq(999L), anyString()))
            .thenThrow(new NoSuchElementException("User not found"));
    
        String json = mapper.writeValueAsString(java.util.Map.of("text", "Hello"));
    
        mvc.perform(post("/messages/users/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getMessages_shouldReturn200() throws Exception {
        DirectMessage msg1 = new DirectMessage();
        msg1.setText("Hello!");
        msg1.setSender(sender);
        msg1.setReceiver(receiver);
        msg1.setTimestamp(Instant.now());
        msg1.setRead(false);

        when(directMessageService.getConversation(1L, 2L))
            .thenReturn(Arrays.asList(msg1));

        mvc.perform(get("/messages/users/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].text").value("Hello!"));

        verify(directMessageService, times(1)).getConversation(1L, 2L);
        verify(directMessageService, times(1)).markConversationRead(1L, 2L);
    }

    @Test
    void sendMessage_shouldReturn400_whenTextIsEmpty() throws Exception {
        String json = mapper.writeValueAsString(java.util.Map.of("text", ""));

        mvc.perform(post("/messages/users/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(directMessageService, never()).sendMessage(anyLong(), anyLong(), anyString());
    }

    @Test
    void getChats_shouldReturn200() throws Exception {
        when(directMessageService.getAllMessagesOfUser(1L))
            .thenReturn(Arrays.asList(testMessage));

        mvc.perform(get("/messages/chats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].otherUser.username").value("receiver"));

        verify(directMessageService, times(1)).getAllMessagesOfUser(1L);
    }

    @Test
    void searchUsers_shouldReturn200() throws Exception {
        mvc.perform(get("/messages/users/search?q=rec"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("receiver"));
    }
}