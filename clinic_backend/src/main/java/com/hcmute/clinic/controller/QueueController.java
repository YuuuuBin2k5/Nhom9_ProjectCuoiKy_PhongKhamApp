package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public class QueueController {

    private final CheckInQueueService checkInQueueService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomQueue(@PathVariable Long roomId) {
        return ResponseEntity.ok(checkInQueueService.getRoomQueue(roomId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "status is required"));
        }
        try {
            checkInQueueService.updateStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/call")
    public ResponseEntity<?> callToRoom(@PathVariable Long id) {
        try {
            checkInQueueService.callToRoom(id);
            return ResponseEntity.ok(Map.of("message", "Đã gọi bệnh nhân vào phòng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/transfer-xray")
    public ResponseEntity<?> transferToXRay(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> body) {
        Long xRayRoomId = body != null && body.containsKey("xRayRoomId") ? body.get("xRayRoomId") : null;
        try {
            checkInQueueService.transferToXRay(id, xRayRoomId);
            return ResponseEntity.ok(Map.of("message", "Đã chuyển đi chụp X-Quang"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete-xray")
    public ResponseEntity<?> completeXRay(@PathVariable Long id) {
        try {
            checkInQueueService.completeXRay(id);
            return ResponseEntity.ok(Map.of("message", "Bệnh nhân đã về, ưu tiên"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
