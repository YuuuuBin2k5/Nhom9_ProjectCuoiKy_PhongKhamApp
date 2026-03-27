package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.PrescriptionDTO;
import com.hcmute.clinic.dto.PrescriptionRequest;
import com.hcmute.clinic.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionDTO> createOrUpdatePrescription(
            @RequestBody PrescriptionRequest request,
            Authentication authentication) {
        String doctorEmail = authentication.getName();
        PrescriptionDTO dto = prescriptionService.createPrescription(request, doctorEmail);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<PrescriptionDTO> getPrescriptionByAppointment(@PathVariable Long appointmentId) {
        PrescriptionDTO dto = prescriptionService.getPrescriptionByAppointmentId(appointmentId);
        return ResponseEntity.ok(dto);
    }
}
