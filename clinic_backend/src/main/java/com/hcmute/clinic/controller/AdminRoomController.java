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

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRooms() {
        List<ClinicRoom> rooms = clinicRoomRepository.findAll();
        
        List<Map<String, Object>> roomData = rooms.stream()
                .map(room -> {
                    Map<String, Object> roomMap = new java.util.HashMap<>();
                    roomMap.put("id", room.getId());
                    roomMap.put("name", room.getName() != null ? room.getName() : "Phòng " + room.getId());
                    roomMap.put("status", "Đang hoạt động"); // Default status since no isActive field
                    roomMap.put("waitingCount", 0); // TODO: Calculate actual waiting count from queue
                    return roomMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(roomData);
    }
}