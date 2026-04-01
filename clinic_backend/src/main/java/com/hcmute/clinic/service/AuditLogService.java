package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.AuditLog;
import com.hcmute.clinic.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void log(String userId, String userRole, String action, String entityType, 
                    Long entityId, Object details, String ipAddress) {
        try {
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;
            
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .userRole(userRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(detailsJson)
                    .ipAddress(ipAddress)
                    .build();
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }
    
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
