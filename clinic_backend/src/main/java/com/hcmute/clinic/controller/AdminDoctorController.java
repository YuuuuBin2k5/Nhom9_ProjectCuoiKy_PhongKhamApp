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
    private final com.hcmute.clinic.repository.DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll().stream()
                .map(d -> Map.of(
                        "id", d.getId(),
                        "firstName", d.getFirstName() != null ? d.getFirstName() : "",
                        "lastName", d.getLastName() != null ? d.getLastName() : "",
                        "email", d.getEmail() != null ? d.getEmail() : "",
                        "specialization", d.getSpecialization() != null ? d.getSpecialization() : "",
                        "roomName", d.getClinicRoom() != null ? d.getClinicRoom().getName() : "",
                        "experienceYears", d.getExperienceYears() != null ? d.getExperienceYears() : 0,
                        "active", d.isActive(),
                        "avatarUrl", d.getAvatarUrl() != null ? d.getAvatarUrl() : ""
                ))
                .toList());
    }

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

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateDoctorStatus(@PathVariable("id") Long id, @RequestParam boolean active) {
        try {
            adminDoctorService.updateDoctorStatus(id, active);
            return ResponseEntity.ok(Map.of("message", "Doctor status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
