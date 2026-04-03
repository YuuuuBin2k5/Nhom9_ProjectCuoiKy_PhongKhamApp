package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller điều phối hàng đợi khám bệnh (UC_06).
 * Hỗ trợ bác sĩ gọi bệnh nhân, chuyển tuyến và quản lý trạng thái chờ.
 */
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public class QueueController {

    private final CheckInQueueService checkInQueueService;

    /**
     * Lấy danh sách hàng đợi của một phòng khám cụ thể.
     * @param roomId ID của phòng khám.
     * @return Danh sách bệnh nhân đang chờ tại phòng.
     */
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
    
    @PostMapping("/{id}/transfer-surgery")
    public ResponseEntity<?> transferToSurgery(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> body) {
        Long surgeryRoomId = body != null && body.containsKey("surgeryRoomId") ? body.get("surgeryRoomId") : null;
        try {
            checkInQueueService.transferToSurgery(id, surgeryRoomId);
            return ResponseEntity.ok(Map.of("message", "Đã chuyển đi Tiểu phẫu"));
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

    @PostMapping("/{id}/delay")
    public ResponseEntity<?> delayPatient(@PathVariable Long id) {
        try {
            checkInQueueService.delayPatient(id);
            return ResponseEntity.ok(Map.of("message", "Đã đẩy lùi bệnh nhân xuống 1 vị trí"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<?> skipCurrentPatient(@PathVariable Long id) {
        try {
            checkInQueueService.skipCurrentPatient(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lùi bệnh nhân và gọi người tiếp theo"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
