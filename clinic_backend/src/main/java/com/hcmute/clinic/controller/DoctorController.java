package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public class DoctorController {

    private static final Pattern QR_PATTERN = Pattern.compile("^patient:(\\d+)$");

    private final PatientRepository patientRepository;

    @GetMapping("/patient")
    public ResponseEntity<?> getPatientByQr(@RequestParam String qr) {
        if (qr == null || qr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "QR data is required"));
        }
        Matcher m = QR_PATTERN.matcher(qr.trim());
        if (!m.matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã QR không hợp lệ"));
        }
        long patientId = Long.parseLong(m.group(1));
        return patientRepository.findById(patientId)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(Map.of(
                        "id", p.getId(),
                        "firstName", p.getFirstName() != null ? p.getFirstName() : "",
                        "lastName", p.getLastName() != null ? p.getLastName() : "",
                        "email", p.getEmail() != null ? p.getEmail() : "",
                        "phone", p.getPhone() != null ? p.getPhone() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
