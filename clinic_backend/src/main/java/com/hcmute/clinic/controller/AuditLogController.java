package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.AuditLog;
import com.hcmute.clinic.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    
    private final AuditLogRepository auditLogRepository;
    
    @GetMapping
    public ResponseEntity<?> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", logs.getContent());
        response.put("page", logs.getNumber());
        response.put("size", logs.getSize());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        response.put("last", logs.isLast());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAuditLogsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", logs.getContent());
        response.put("page", logs.getNumber());
        response.put("size", logs.getSize());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        response.put("last", logs.isLast());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/entity/{entityType}")
    public ResponseEntity<?> getAuditLogsByEntity(
            @PathVariable String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", logs.getContent());
        response.put("page", logs.getNumber());
        response.put("size", logs.getSize());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        response.put("last", logs.isLast());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/action/{action}")
    public ResponseEntity<?> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", logs.getContent());
        response.put("page", logs.getNumber());
        response.put("size", logs.getSize());
        response.put("totalElements", logs.getTotalElements());
        response.put("totalPages", logs.getTotalPages());
        response.put("last", logs.isLast());
        
        return ResponseEntity.ok(response);
    }
}
