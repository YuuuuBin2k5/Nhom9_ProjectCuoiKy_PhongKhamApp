package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInMyStatusResponse;
import com.hcmute.clinic.dto.PatientMeResponse;
import com.hcmute.clinic.dto.UpdatePatientRequest;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.Patient;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
                        .phone(p.getPhone())
                        .address(p.getAddress())
                        .gender(p.getGender())
                        .dob(p.getDob() != null ? p.getDob().toString() : null)
                        .avatarUrl(p.getAvatarUrl())
                        .allergies(p.getProfile() != null ? p.getProfile().getAllergies() : null)
                        .underlyingConditions(p.getProfile() != null ? p.getProfile().getUnderlyingConditions() : null)
                        .bloodType(p.getProfile() != null ? p.getProfile().getBloodType() : null)
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/fcm-token")
    @jakarta.transaction.Transactional
    public ResponseEntity<?> updateFcmToken(@RequestBody java.util.Map<String, String> body, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        return patientRepository.findById(patientId).map(p -> {
            p.setFcmToken(body.get("token"));
            patientRepository.save(p);
            return ResponseEntity.ok(Map.of("message", "FCM token updated successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @jakarta.transaction.Transactional
    public ResponseEntity<?> updateMe(@RequestBody UpdatePatientRequest req, Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        
        // Validate phone number format (Vietnam)
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            String phone = req.getPhone().trim();
            if (!phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", 
                    "Số điện thoại không hợp lệ. Định dạng: 0xxxxxxxxx hoặc +84xxxxxxxxx"));
            }
        }
        
        // Validate date of birth
        if (req.getDob() != null && !req.getDob().isBlank()) {
            try {
                java.time.LocalDate dob = java.time.LocalDate.parse(req.getDob());
                if (dob.isAfter(java.time.LocalDate.now())) {
                    return ResponseEntity.badRequest().body(Map.of("message", 
                        "Ngày sinh không thể là ngày trong tương lai"));
                }
                if (dob.isBefore(java.time.LocalDate.now().minusYears(120))) {
                    return ResponseEntity.badRequest().body(Map.of("message", 
                        "Ngày sinh không hợp lệ"));
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", 
                    "Định dạng ngày sinh không hợp lệ. Sử dụng: YYYY-MM-DD"));
            }
        }
        
        long patientId = Long.parseLong(auth.getName());
        return patientRepository.findById(patientId).map(p -> {
            if (req.getFirstName() != null && !req.getFirstName().isBlank()) p.setFirstName(req.getFirstName().trim());
            if (req.getLastName() != null && !req.getLastName().isBlank()) p.setLastName(req.getLastName().trim());
            if (req.getPhone() != null) p.setPhone(req.getPhone().trim());
            if (req.getAddress() != null) p.setAddress(req.getAddress().trim());
            if (req.getGender() != null) p.setGender(req.getGender());
            if (req.getAvatarUrl() != null) p.setAvatarUrl(req.getAvatarUrl().trim());
            if (req.getDob() != null && !req.getDob().isBlank()) {
                try { p.setDob(java.time.LocalDate.parse(req.getDob())); } catch (Exception ignored) {}
            }
            
            // Handle PatientProfile fields
            if (p.getProfile() == null) {
                com.hcmute.clinic.entity.PatientProfile newProfile = com.hcmute.clinic.entity.PatientProfile.builder()
                        .patient(p)
                        .build();
                p.setProfile(newProfile);
            }
            if (req.getAllergies() != null) p.getProfile().setAllergies(req.getAllergies().trim());
            if (req.getUnderlyingConditions() != null) p.getProfile().setUnderlyingConditions(req.getUnderlyingConditions().trim());
            if (req.getBloodType() != null) p.getProfile().setBloodType(req.getBloodType().trim());

            try {
                patientRepository.saveAndFlush(p);
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("message", "Database error: " + e.getMessage()));
            }

            return ResponseEntity.ok(PatientMeResponse.builder()
                    .id(p.getId())
                    .email(p.getEmail())
                    .firstName(p.getFirstName())
                    .lastName(p.getLastName())
                    .qrCodeData(p.getQrCodeData())
                    .phone(p.getPhone())
                    .address(p.getAddress())
                    .gender(p.getGender())
                    .dob(p.getDob() != null ? p.getDob().toString() : null)
                    .avatarUrl(p.getAvatarUrl())
                    .allergies(p.getProfile() != null ? p.getProfile().getAllergies() : null)
                    .underlyingConditions(p.getProfile() != null ? p.getProfile().getUnderlyingConditions() : null)
                    .bloodType(p.getProfile() != null ? p.getProfile().getBloodType() : null)
                    .build());
        }).orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<?> myMedicalRecords(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by("createdAt").descending());
        
        org.springframework.data.domain.Page<MedicalRecord> recordPage = 
            medicalRecordRepository.findByPatientId(patientId, pageable);
        
        List<Map<String, Object>> items = recordPage.getContent().stream()
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
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", items);
        response.put("page", recordPage.getNumber());
        response.put("size", recordPage.getSize());
        response.put("totalElements", recordPage.getTotalElements());
        response.put("totalPages", recordPage.getTotalPages());
        response.put("last", recordPage.isLast());
        
        return ResponseEntity.ok(response);
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
