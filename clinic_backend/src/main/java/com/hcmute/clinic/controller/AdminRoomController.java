package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.RoomRequest;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.enums.QueueStatus;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import com.hcmute.clinic.service.QueueEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomController {

    private final ClinicRoomRepository clinicRoomRepository;
    private final CheckInQueueRepository checkInQueueRepository;
    private final QueueEventService queueEventService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRooms() {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);

        return ResponseEntity.ok(clinicRoomRepository.findAll().stream()
                .map(room -> {
                    long waitingCount = checkInQueueRepository.findAll().stream()
                            .filter(q -> q.getClinicRoom().getId().equals(room.getId()))
                            .filter(q -> q.getStatus() == QueueStatus.WAITING)
                            .filter(q -> q.getCheckInTime() != null && q.getCheckInTime().isAfter(startOfDay) && q.getCheckInTime().isBefore(endOfDay))
                            .count();

                    Map<String, Object> roomMap = new java.util.HashMap<>();
                    roomMap.put("id", room.getId());
                    roomMap.put("name", room.getName());
                    roomMap.put("active", room.isActive());
                    roomMap.put("waitingCount", (int) waitingCount);
                    return roomMap;
                })
                .collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateRoomStatus(@PathVariable Long id, @RequestParam boolean active) {
        return clinicRoomRepository.findById(id)
                .map(room -> {
                    room.setActive(active);
                    clinicRoomRepository.save(room);
                    return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest request) {
        ClinicRoom room = new ClinicRoom();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setActive(true);
        room = clinicRoomRepository.save(room);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", room.getId());
        response.put("name", room.getName());
        response.put("description", room.getDescription());
        response.put("active", room.isActive());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return clinicRoomRepository.findById(id)
                .map(room -> {
                    room.setName(request.getName());
                    room.setDescription(request.getDescription());
                    clinicRoomRepository.save(room);

                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("id", room.getId());
                    response.put("name", room.getName());
                    response.put("description", room.getDescription());
                    response.put("active", room.isActive());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        return clinicRoomRepository.findById(id)
                .map(room -> {
                    // Soft delete
                    room.setActive(false);
                    clinicRoomRepository.save(room);
                    return ResponseEntity.ok(Map.of("message", "Đã xóa phòng"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * UC-27: Admin loại bỏ bệnh nhân WAITING ra khỏi hàng đợi.
     * Theo SE_27: cập nhật status → CANCELLED, broadcast event.
     * Bệnh nhân bị hủy phải check-in lại mới vào được hàng đợi.
     */
    @DeleteMapping("/{roomId}/queue/{queueId}")
    public ResponseEntity<?> removePatientFromQueue(
            @PathVariable Long roomId,
            @PathVariable Long queueId) {

        // 1. Kiểm tra phòng tồn tại
        clinicRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng khám"));

        // 2. Kiểm tra queue tồn tại và thuộc phòng này
        CheckInQueue queue = checkInQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bệnh nhân trong hàng đợi"));

        if (queue.getClinicRoom() == null || !queue.getClinicRoom().getId().equals(roomId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bệnh nhân không thuộc phòng này");
        }

        // 3. Chỉ cho phép hủy bệnh nhân đang WAITING
        if (queue.getStatus() != QueueStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ có thể loại bỏ bệnh nhân đang ở trạng thái chờ (WAITING). Trạng thái hiện tại: " + queue.getStatus());
        }

        // 4. Cập nhật trạng thái → CANCELLED
        queue.setStatus(QueueStatus.CANCELLED);
        checkInQueueRepository.save(queue);

        // 5. Broadcast cập nhật hàng đợi real-time
        try {
            queueEventService.broadcastQueueUpdated(roomId);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of("message", "Đã loại bỏ bệnh nhân khỏi hàng đợi"));
    }
}