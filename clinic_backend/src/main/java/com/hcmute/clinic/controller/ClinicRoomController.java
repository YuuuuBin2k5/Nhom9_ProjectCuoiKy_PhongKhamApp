package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.ClinicRoom;
import com.hcmute.clinic.repository.ClinicRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller truy xuất thông tin các phòng chức năng trong hệ thống.
 */
@RestController
@RequestMapping("/api/clinic-rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public class ClinicRoomController {

    private final ClinicRoomRepository clinicRoomRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        List<Map<String, Object>> rooms = clinicRoomRepository.findAll().stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "name", r.getName() != null ? r.getName() : "",
                        "description", r.getDescription() != null ? r.getDescription() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }
}
