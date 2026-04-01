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
    public ResponseEntity<?> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort
    ) {
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(sort));
        
        // @EntityGraph trong repository sẽ tự động fetch clinicRoom, tránh N+1 query
        org.springframework.data.domain.Page<Doctor> doctorPage = doctorRepository.findAll(pageable);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", doctorPage.getContent().stream()
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
        response.put("page", doctorPage.getNumber());
        response.put("size", doctorPage.getSize());
        response.put("totalElements", doctorPage.getTotalElements());
        response.put("totalPages", doctorPage.getTotalPages());
        response.put("last", doctorPage.isLast());
        
        return ResponseEntity.ok(response);
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable("id") Long id, @RequestBody CreateDoctorRequest request) {
        try {
            Doctor doctor = adminDoctorService.updateDoctor(id, request);
            return ResponseEntity.ok(Map.of(
                    "id", doctor.getId(),
                    "email", doctor.getEmail(),
                    "message", "Doctor updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable("id") Long id) {
        try {
            adminDoctorService.deleteDoctor(id);
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
