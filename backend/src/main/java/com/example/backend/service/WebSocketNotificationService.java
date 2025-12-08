package com.example.backend.service;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.model.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketNotificationService {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void registerSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregisterSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public void notifyUser(Long userId, Notification notification) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            NotificationDTO dto = new NotificationDTO(notification);
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "notification");
            message.put("data", dto);

            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : new ArrayList<>(sessions)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    } else {
                        sessions.remove(session);
                    }
                } catch (IOException e) {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            // log error
        }
    }

    public void notifyUnreadCount(Long userId, long count) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "unread-count");
            Map<String, Long> data = new LinkedHashMap<>();
            data.put("count", count);
            message.put("data", data);

            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : new ArrayList<>(sessions)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    } else {
                        sessions.remove(session);
                    }
                } catch (IOException e) {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            // log error
        }
    }
}