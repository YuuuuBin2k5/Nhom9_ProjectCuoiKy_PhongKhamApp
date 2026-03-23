package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInMyStatusResponse;
import com.hcmute.clinic.dto.PatientMeResponse;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final CheckInQueueService checkInQueueService;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/me")
    public ResponseEntity<PatientMeResponse> me(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        return patientRepository.findById(patientId)
                .map(p -> ResponseEntity.ok(PatientMeResponse.builder()
                        .id(p.getId())
                        .email(p.getEmail())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .qrCodeData(p.getQrCodeData())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/checkin-status")
    public ResponseEntity<CheckInMyStatusResponse> myCheckInStatus(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        if (!patientRepository.existsById(patientId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(checkInQueueService.getMyStatusToday(patientId));
    }

    @GetMapping("/me/appointments/upcoming")
    public ResponseEntity<?> myUpcomingAppointments(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        List<Appointment> list = appointmentRepository.findUpcomingByPatientId(patientId, 7)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .collect(Collectors.toList());
        List<Map<String, Object>> items = list.stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "datetime", a.getAppointmentDatetime() != null ? a.getAppointmentDatetime().toString() : "",
                        "serviceName", a.getService() != null ? a.getService().getName() : "",
                        "doctorName", a.getDoctor() != null ? (a.getDoctor().getLastName() + " " + a.getDoctor().getFirstName()).trim() : "",
                        "status", a.getStatus() != null ? a.getStatus().name() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }
}
