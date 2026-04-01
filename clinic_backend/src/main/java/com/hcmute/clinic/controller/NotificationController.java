package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Notification;
import com.hcmute.clinic.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/me")
    public ResponseEntity<?> myNotifications(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId;
        try {
            patientId = Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> list = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        List<Map<String, Object>> items = list.stream()
                .map(n -> Map.<String, Object>of(
                        "id", n.getId(),
                        "title", n.getTitle() != null ? n.getTitle() : "",
                        "message", n.getMessage() != null ? n.getMessage() : "",
                        "type", n.getType() != null ? n.getType() : "",
                        "isRead", n.isRead(),
                        "createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
                ))
                .toList();
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        return notificationRepository.findById(id)
                .filter(n -> n.getPatient() != null && n.getPatient().getId().equals(patientId))
                .map(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                    return ResponseEntity.ok(Map.of("message", "Đã đọc"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        List<Notification> unreadNotifications = notificationRepository
            .findByPatientIdAndIsReadOrderByCreatedAtDesc(patientId, false);
        
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        
        return ResponseEntity.ok(Map.of(
            "message", "Đã đánh dấu tất cả là đã đọc",
            "count", unreadNotifications.size()
        ));
    }
}
