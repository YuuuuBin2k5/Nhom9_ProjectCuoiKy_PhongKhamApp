package com.hcmute.clinic.websocket;

import com.hcmute.clinic.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }
        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = jwtService.parseClaims(token);
            String role = claims.get("role", String.class);
            if (role == null) {
                role = "PATIENT";
            }
            long userId = Long.parseLong(claims.getSubject());
            attributes.put("userId", userId);
            attributes.put("role", role);
            if ("PATIENT".equals(role)) {
                String did = req.getParameter("doctorId");
                if (did == null) {
                    return false;
                }
                attributes.put("doctorId", Long.parseLong(did));
            } else if ("DOCTOR".equals(role)) {
                String pid = req.getParameter("patientId");
                if (pid == null) {
                    return false;
                }
                attributes.put("patientId", Long.parseLong(pid));
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
