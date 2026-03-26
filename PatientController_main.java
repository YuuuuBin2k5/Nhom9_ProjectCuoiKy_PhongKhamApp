package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInMyStatusResponse;
import com.hcmute.clinic.dto.PatientMeResponse;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.entity.MedicalRecord;
import com.hcmute.clinic.entity.Prescription;
import com.hcmute.clinic.repository.MedicalRecordRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.repository.PrescriptionRepository;
import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final CheckInQueueService checkInQueueService;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;

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

    @GetMapping("/me/medical-records")
    public ResponseEntity<?> myMedicalRecords(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        List<MedicalRecord> list = medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        List<Map<String, Object>> items = list.stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "date", m.getCreatedAt() != null ? m.getCreatedAt().toString() : "",
                        "diagnosis", m.getDiagnosis() != null ? m.getDiagnosis() : "",
                        "doctorName", m.getDoctor() != null ? (m.getDoctor().getLastName() + " " + m.getDoctor().getFirstName()).trim() : "",
                        "doctorSpecialty", m.getDoctor() != null && m.getDoctor().getSpecialization() != null ? m.getDoctor().getSpecialization() : "Nha sĩ",
                        "symptoms", m.getSymptoms() != null ? m.getSymptoms() : "",
                        "advice", m.getAdvice() != null ? m.getAdvice() : "",
                        "prescription", m.getPrescription() != null ? Map.of(
                                "id", m.getPrescription().getId(),
                                "details", m.getPrescription().getDetails() != null ? m.getPrescription().getDetails().stream()
                                        .map(d -> Map.of(
                                                "medicineName", d.getMedicineName(),
                                                "dosage", d.getDosage() != null ? d.getDosage() : ""
                                        )).collect(Collectors.toList()) : List.of()
                        ) : Map.of()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/me/medical-records/{id}")
    public ResponseEntity<?> medicalRecordDetail(@PathVariable Long id, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        return medicalRecordRepository.findById(id)
                .filter(m -> m.getPatient().getId().equals(patientId))
                .map(m -> Map.ofEntries(
                        entry("id", m.getId()),
                        entry("date", m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""),
                        entry("diagnosis", m.getDiagnosis() != null ? m.getDiagnosis() : ""),
                        entry("doctorName", m.getDoctor() != null ? (m.getDoctor().getLastName() + " " + m.getDoctor().getFirstName()).trim() : ""),
                        entry("doctorSpecialty", m.getDoctor() != null && m.getDoctor().getSpecialization() != null ? m.getDoctor().getSpecialization() : "Nha sĩ"),
                        entry("symptoms", m.getSymptoms() != null ? m.getSymptoms() : ""),
                        entry("bloodPressure", m.getBloodPressure() != null ? m.getBloodPressure() : ""),
                        entry("heartRate", m.getHeartRate() != null ? m.getHeartRate() : 0),
                        entry("advice", m.getAdvice() != null ? m.getAdvice() : ""),
                        entry("prescriptionId", m.getPrescription() != null ? m.getPrescription().getId() : 0L),
                        entry("details", m.getDetails() != null ? m.getDetails().stream()
                                .map(d -> Map.of(
                                        "serviceName", d.getService() != null ? d.getService().getName() : "",
                                        "toothNumber", d.getToothNumber() != null ? d.getToothNumber() : "",
                                        "note", d.getTreatmentNote() != null ? d.getTreatmentNote() : ""
                                )).collect(Collectors.toList()) : List.of())
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/prescriptions/{id}")
    public ResponseEntity<?> prescriptionDetail(@PathVariable Long id, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        return prescriptionRepository.findById(id)
                .filter(p -> p.getMedicalRecord().getPatient().getId().equals(patientId))
                .map(p -> ResponseEntity.ok(Map.of(
                        "id", p.getId(),
                        "doctorName", p.getDoctor() != null ? (p.getDoctor().getLastName() + " " + p.getDoctor().getFirstName()).trim() : "",
                        "date", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "",
                        "details", p.getDetails() != null ? p.getDetails().stream()
                                .map(d -> Map.of(
                                        "medicineName", d.getMedicineName(),
                                        "dosage", d.getDosage() != null ? d.getDosage() : "",
                                        "frequency", d.getFrequency() != null ? d.getFrequency() : "",
                                        "duration", d.getDuration() != null ? d.getDuration() : "",
                                        "unit", d.getUnit() != null ? d.getUnit() : ""
                                )).collect(Collectors.toList()) : List.of()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
