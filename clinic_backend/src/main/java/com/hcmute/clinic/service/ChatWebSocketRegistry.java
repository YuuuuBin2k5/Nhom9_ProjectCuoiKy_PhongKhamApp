package com.hcmute.clinic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class ChatWebSocketRegistry {

    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public void joinRoom(String roomKey, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomKey, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void leaveRoom(String roomKey, WebSocketSession session) {
        Set<WebSocketSession> set = roomSessions.get(roomKey);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                roomSessions.remove(roomKey);
            }
        }
    }

    public void broadcast(String roomKey, String json) {
        Set<WebSocketSession> set = roomSessions.get(roomKey);
        if (set == null || set.isEmpty()) {
            return;
        }
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.sendMessage(msg);
                    }
                } catch (IOException e) {
                    log.warn("WebSocket send failed: {}", e.getMessage());
                }
            }
        }
    }
}
