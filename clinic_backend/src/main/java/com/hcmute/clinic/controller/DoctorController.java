package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
public class DoctorController {

    private static final Pattern QR_PATTERN = Pattern.compile("^patient:(\\d+)$");

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

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
        
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Patient p = patientOpt.get();
        
        // Find today's appointment or most recent scheduled/confirmed one
        List<Appointment> todayApps = appointmentRepository.findTodayByPatientId(patientId);
        String serviceName = "";
        String status = "NONE";
        if (!todayApps.isEmpty()) {
            serviceName = todayApps.get(0).getService().getName();
            status = todayApps.get(0).getStatus().name();
        } else {
            // Fallback: Check for any upcoming/recent appointment
            Optional<Appointment> recent = appointmentRepository.findFirstByPatientIdAndStatusInOrderByAppointmentDatetimeDesc(
                patientId, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS));
            if (recent.isPresent()) {
                serviceName = recent.get().getService().getName();
                status = recent.get().getStatus().name();
            }
        }

        return ResponseEntity.ok(Map.of(
                "id", p.getId(),
                "firstName", p.getFirstName() != null ? p.getFirstName() : "",
                "lastName", p.getLastName() != null ? p.getLastName() : "",
                "email", p.getEmail() != null ? p.getEmail() : "",
                "phone", p.getPhone() != null ? p.getPhone() : "",
                "bookedService", serviceName,
                "appointmentStatus", status
        ));
    }
}
