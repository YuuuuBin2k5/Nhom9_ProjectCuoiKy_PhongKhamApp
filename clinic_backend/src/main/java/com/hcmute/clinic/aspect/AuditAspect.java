package com.hcmute.clinic.aspect;

import com.hcmute.clinic.annotation.Auditable;
import com.hcmute.clinic.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditLogService auditLogService;
    
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";
            String userRole = auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty() 
                            ? auth.getAuthorities().iterator().next().getAuthority() 
                            : "UNKNOWN";
            
            // Get IP address
            String ipAddress = "unknown";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = auditLogService.getClientIp(request);
            }
            
            // Extract entity ID from result if possible
            Long entityId = extractEntityId(result);
            
            // Create details map
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().getName());
            details.put("args", joinPoint.getArgs());
            
            auditLogService.log(
                userId,
                userRole,
                auditable.action(),
                auditable.entityType(),
                entityId,
                details,
                ipAddress
            );
        } catch (Exception e) {
            // Don't fail the main operation if audit logging fails
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }
    
    private Long extractEntityId(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            // Try to get ID from common response patterns
            if (result instanceof org.springframework.http.ResponseEntity) {
                Object body = ((org.springframework.http.ResponseEntity<?>) result).getBody();
                if (body instanceof Map) {
                    Object id = ((Map<?, ?>) body).get("id");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore extraction errors
        }
        
        return null;
    }
}
