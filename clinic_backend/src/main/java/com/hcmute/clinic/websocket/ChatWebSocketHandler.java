package com.hcmute.clinic.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmute.clinic.service.ChatService;
import com.hcmute.clinic.service.ChatWebSocketRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatWebSocketRegistry registry;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> a = session.getAttributes();
        String role = (String) a.get("role");
        Long userId = (Long) a.get("userId");
        String roomKey;
        if ("PATIENT".equals(role)) {
            Long doctorId = (Long) a.get("doctorId");
            roomKey = ChatService.roomKey(userId, doctorId);
        } else if ("DOCTOR".equals(role)) {
            Long patientId = (Long) a.get("patientId");
            roomKey = ChatService.roomKey(patientId, userId);
        } else {
            return;
        }
        a.put("roomKey", roomKey);
        registry.joinRoom(roomKey, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> a = session.getAttributes();
        String role = (String) a.get("role");
        Long userId = (Long) a.get("userId");
        JsonNode n = objectMapper.readTree(message.getPayload());
        String content = n.has("content") ? n.get("content").asText() : "";
        if ("PATIENT".equals(role)) {
            Long doctorId = (Long) a.get("doctorId");
            chatService.sendFromPatient(userId, doctorId, content);
        } else if ("DOCTOR".equals(role)) {
            Long patientId = (Long) a.get("patientId");
            chatService.sendFromDoctor(userId, patientId, content);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomKey = (String) session.getAttributes().get("roomKey");
        if (roomKey != null) {
            registry.leaveRoom(roomKey, session);
        }
    }
}
