package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomController {

    private final ClinicRoomRepository clinicRoomRepository;
    private final com.hcmute.clinic.repository.CheckInQueueRepository checkInQueueRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRooms() {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return ResponseEntity.ok(clinicRoomRepository.findAll().stream()
                .map(room -> {
                    long waitingCount = checkInQueueRepository.findAll().stream()
                            .filter(q -> q.getClinicRoom().getId().equals(room.getId()))
                            .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING)
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
}