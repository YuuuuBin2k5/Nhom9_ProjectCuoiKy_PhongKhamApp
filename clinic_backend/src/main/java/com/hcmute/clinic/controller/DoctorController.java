package com.hcmute.clinic.controller;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.entity.CheckInQueue;
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

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final CheckInQueueRepository checkInQueueRepository;
    private final com.hcmute.clinic.repository.DoctorRepository doctorRepository;
    private final com.hcmute.clinic.repository.TreatmentPlanRepository treatmentPlanRepository;
    private final com.hcmute.clinic.service.CheckInQueueService checkInQueueService;
    private final com.hcmute.clinic.security.JwtService jwtService;

    @GetMapping("/me/queue")
    public ResponseEntity<?> getMyQueue(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long doctorId = Long.parseLong(auth.getName());
        com.hcmute.clinic.entity.Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getClinicRoom() == null) {
            return ResponseEntity.ok(List.of()); // No room or doctor -> empty queue
        }
        return ResponseEntity.ok(checkInQueueService.getDoctorDashboardQueue(doctor.getClinicRoom().getId()));
    }

    @GetMapping("/me/appointments/upcoming")
    public ResponseEntity<?> getMyAppointments(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long doctorId = Long.parseLong(auth.getName());
        List<Appointment> list = appointmentRepository.findTodayByDoctorId(doctorId).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.IN_PROGRESS)
                .collect(java.util.stream.Collectors.toList());
                
        List<Map<String, Object>> items = list.stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "datetime", a.getAppointmentDatetime() != null ? a.getAppointmentDatetime().toString() : "",
                        "serviceName", a.getService() != null ? a.getService().getName() : "",
                        "patientName", a.getPatient() != null ? (a.getPatient().getLastName() + " " + a.getPatient().getFirstName()).trim() : "",
                        "status", a.getStatus() != null ? a.getStatus().name() : ""
                ))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/patient")
    public ResponseEntity<?> getPatientByQr(@RequestParam String qr) {
        if (qr == null || qr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "QR data is required"));
        }
        
        long patientId = -1;
        Long appointmentIdFromQr = null;
        
        qr = qr.trim();
        if (qr.startsWith("eyJ")) {
            try {
                io.jsonwebtoken.Claims claims = jwtService.parseClaims(qr);
                patientId = Long.parseLong(claims.getSubject());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã QR JWT không hợp lệ"));
            }
        } else if (qr.startsWith("CHECKIN:")) {
            try {
                appointmentIdFromQr = Long.parseLong(qr.split(":")[1].trim());
                Appointment appt = appointmentRepository.findById(appointmentIdFromQr)
                        .orElseThrow(() -> new RuntimeException());
                patientId = appt.getPatient().getId();
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã CHECKIN không hợp lệ"));
            }
        } else if (qr.matches("^patient:(\\d+)$")) {
            patientId = Long.parseLong(qr.replaceAll("patient:", ""));
        } else {
            try {
                long numericId = Long.parseLong(qr);
                var appt = appointmentRepository.findById(numericId);
                if (appt.isPresent()) {
                    appointmentIdFromQr = appt.get().getId();
                    patientId = appt.get().getPatient().getId();
                } else {
                    patientId = numericId;
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mã không hợp lệ"));
            }
        }
        
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Patient p = patientOpt.get();
        
        // Find today's appointment or most recent scheduled/confirmed one
        List<Appointment> todayApps = appointmentRepository.findTodayByPatientId(patientId);
        String serviceName = "";
        String status = "NONE";
        Long queueId = null;
        if (!todayApps.isEmpty()) {
            serviceName = todayApps.get(0).getService().getName();
            status = todayApps.get(0).getStatus().name();
            Optional<CheckInQueue> q = checkInQueueRepository.findByAppointmentId(todayApps.get(0).getId());
            if (q.isPresent()) {
                queueId = q.get().getId();
            }
        } else {
            // Fallback: Check for any upcoming/recent appointment
            Optional<Appointment> recent = appointmentRepository.findFirstByPatientIdAndStatusInOrderByAppointmentDatetimeDesc(
                patientId, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS));
            if (recent.isPresent()) {
                serviceName = recent.get().getService().getName();
                status = recent.get().getStatus().name();
                Optional<CheckInQueue> q = checkInQueueRepository.findByAppointmentId(recent.get().getId());
                if (q.isPresent()) {
                    queueId = q.get().getId();
                }
            }
        }

        Long finalAppointmentId = appointmentIdFromQr;

        if (finalAppointmentId == null) {
            if (!todayApps.isEmpty()) {
                finalAppointmentId = todayApps.get(0).getId();
            } else {
                Optional<Appointment> recent = appointmentRepository.findFirstByPatientIdAndStatusInOrderByAppointmentDatetimeDesc(
                    patientId, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS));
                if (recent.isPresent()) {
                    finalAppointmentId = recent.get().getId();
                }
            }
        }

        // FIX 1: Tìm TreatmentPlan liên kết với appointment
        Long treatmentPlanId = null;
        String treatmentPlanStatus = "NONE";
        boolean hasTreatmentPlan = false;
        
        if (finalAppointmentId != null) {
            Optional<com.hcmute.clinic.entity.TreatmentPlan> planOpt = treatmentPlanRepository
                .findFirstByAppointmentIdOrderByCreatedAtDesc(finalAppointmentId);
            
            if (planOpt.isPresent()) {
                com.hcmute.clinic.entity.TreatmentPlan plan = planOpt.get();
                treatmentPlanId = plan.getId();
                treatmentPlanStatus = plan.getStatus().name();
                hasTreatmentPlan = true;
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", p.getId());
        response.put("firstName", p.getFirstName() != null ? p.getFirstName() : "");
        response.put("lastName", p.getLastName() != null ? p.getLastName() : "");
        response.put("email", p.getEmail() != null ? p.getEmail() : "");
        response.put("phone", p.getPhone() != null ? p.getPhone() : "");
        response.put("bookedService", serviceName);
        response.put("appointmentStatus", status);
        response.put("queueId", queueId != null ? queueId : -1);
        response.put("appointmentId", finalAppointmentId != null ? finalAppointmentId : -1);
        response.put("treatmentPlanId", treatmentPlanId != null ? treatmentPlanId : -1);
        response.put("hasTreatmentPlan", hasTreatmentPlan);
        response.put("treatmentPlanStatus", treatmentPlanStatus);
        
        return ResponseEntity.ok(response);
    }
}
