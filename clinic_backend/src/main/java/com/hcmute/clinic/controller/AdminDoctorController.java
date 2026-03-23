package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CreateDoctorRequest;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.service.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    @PostMapping
    public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest request) {
        try {
            Doctor doctor = adminDoctorService.createDoctor(request);
            return ResponseEntity.ok(Map.of(
                    "id", doctor.getId(),
                    "email", doctor.getEmail(),
                    "message", "Doctor created successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
