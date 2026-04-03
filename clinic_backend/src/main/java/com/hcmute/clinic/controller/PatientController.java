package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.CheckInMyStatusResponse;
import com.hcmute.clinic.dto.PatientMeResponse;
import com.hcmute.clinic.dto.UpdatePatientRequest;
import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Invoice;
import com.hcmute.clinic.entity.MedicalRecordDetail;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.entity.PrescriptionDetail;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.entity.MedicalRecord;
import com.hcmute.clinic.entity.Prescription;
import com.hcmute.clinic.entity.StepImage;
import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.repository.InvoiceRepository;
import com.hcmute.clinic.repository.MedicalRecordRepository;
import com.hcmute.clinic.repository.PatientRepository;
import com.hcmute.clinic.repository.PrescriptionRepository;
import com.hcmute.clinic.service.CheckInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.entry;

/**
 * Lớp Điều khiển PatientController - Quản lý hồ sơ và các yêu cầu cá nhân của Bệnh nhân.
 * Hỗ trợ các chức năng tra cứu bệnh án (Medical Records), lịch hẹn sắp tới và trạng thái hàng đợi.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final CheckInQueueService checkInQueueService;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * API Lấy thông tin chi tiết của Bệnh nhân đang đăng nhập.
     */
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
            if (req.getBloodType() != null) {
                String bt = req.getBloodType().trim();
                p.getProfile().setBloodType(bt.isEmpty() ? null : bt);
            }

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

    /**
     * API Xem lịch sử hồ sơ bệnh án (Medical Records) của bệnh nhân.
     * Dữ liệu trả về bao gồm chẩn đoán, đơn thuốc và các bước điều trị chi tiết.
     */
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
        
        // Get medical records with optimized fetching
        List<MedicalRecord> medicalRecords = recordPage.getContent();
        
        if (!medicalRecords.isEmpty()) {
            medicalRecordRepository.fetchDetails(medicalRecords);
            medicalRecordRepository.fetchPrescriptionDetails(medicalRecords);
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        List<Map<String, Object>> items = medicalRecords.stream()
                .map(this::mapMedicalRecordToMap)
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
                .map(this::mapMedicalRecordToMap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> mapMedicalRecordToMap(MedicalRecord record) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("id", record.getId());
        item.put("appointmentId", record.getAppointment() != null ? record.getAppointment().getId() : null);
        item.put("date", record.getCreatedAt() != null ? record.getCreatedAt().format(dateFormatter) : "");
        
        // Doctor info
        if (record.getDoctor() != null) {
            Doctor doctor = record.getDoctor();
            item.put("doctorName", (doctor.getLastName() + " " + doctor.getFirstName()).trim());
            item.put("doctorSpecialty", doctor.getSpecialization() != null ? 
                doctor.getSpecialization() : "Nha khoa tổng quát");
        } else {
            item.put("doctorName", "");
            item.put("doctorSpecialty", "");
        }
        
        // Diagnosis, symptoms, advice
        item.put("diagnosis", record.getDiagnosis() != null ? record.getDiagnosis() : "Khám tổng quát");
        item.put("symptoms", record.getSymptoms() != null ? record.getSymptoms() : "");
        item.put("advice", record.getAdvice() != null ? record.getAdvice() : "");
        item.put("bloodPressure", record.getBloodPressure() != null ? record.getBloodPressure() : "");
        item.put("heartRate", record.getHeartRate() != null ? record.getHeartRate() : 0);
        
        // Prescription
        if (record.getPrescription() != null) {
            Prescription prescription = record.getPrescription();
            Map<String, Object> prescriptionMap = new java.util.HashMap<>();
            prescriptionMap.put("id", prescription.getId());
            
            List<Map<String, Object>> details = new java.util.ArrayList<>();
            if (prescription.getDetails() != null) {
                for (PrescriptionDetail d : prescription.getDetails()) {
                    Map<String, Object> detailMap = new java.util.HashMap<>();
                    detailMap.put("medicineName", d.getMedicineName());
                    detailMap.put("dosage", d.getDosage() != null ? d.getDosage() : "");
                    detailMap.put("frequency", d.getFrequency() != null ? d.getFrequency() : "");
                    detailMap.put("duration", d.getDuration() != null ? d.getDuration() : "");
                    detailMap.put("unit", d.getUnit() != null ? d.getUnit() : "");
                    detailMap.put("quantity", d.getQuantity() != null ? d.getQuantity() : 0);
                    details.add(detailMap);
                }
            }
            prescriptionMap.put("details", details);
            item.put("prescription", prescriptionMap);
        } else {
            item.put("prescription", Map.of());
        }
        
        // Get services from medical record details
        List<String> services = new java.util.ArrayList<>();
        if (record.getDetails() != null) {
            for (MedicalRecordDetail detail : record.getDetails()) {
                if (detail.getService() != null) {
                    String serviceName = detail.getService().getName();
                    if (detail.getToothNumber() != null && !detail.getToothNumber().isEmpty()) {
                        services.add(serviceName + " (Răng " + detail.getToothNumber() + ")");
                    } else {
                        services.add(serviceName);
                    }
                }
            }
        }
        item.put("services", services);
        
        // Treatment Steps - Chi tiết điều trị theo từng bước từ TreatmentPlan
        List<Map<String, Object>> treatmentSteps = new java.util.ArrayList<>();
        if (record.getTreatmentPlan() != null && record.getTreatmentPlan().getSteps() != null) {
            DateTimeFormatter stepDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            record.getTreatmentPlan().getSteps().stream()
                .filter(step -> step.getStatus() == com.hcmute.clinic.enums.StepStatus.COMPLETED)
                .filter(step -> step.getDoctorConclusion() != null && !step.getDoctorConclusion().trim().isEmpty())
                .sorted(java.util.Comparator.comparing(step -> 
                    step.getCompletedAt() != null ? step.getCompletedAt() : java.time.LocalDateTime.MIN))
                .forEach(step -> {
                    Map<String, Object> stepMap = new java.util.HashMap<>();
                    stepMap.put("serviceName", step.getService() != null ? step.getService().getName() : "Dịch vụ");
                    stepMap.put("toothNumber", step.getToothNumber() != null ? step.getToothNumber() : "");
                    stepMap.put("notes", step.getDoctorConclusion());
                    stepMap.put("completedAt", step.getCompletedAt() != null ? 
                        step.getCompletedAt().format(stepDateFormatter) : "");
                    
                    // NEW: Thêm ảnh từ bước điều trị để bệnh nhân có thể quan sát
                    if (step.getImages() != null && !step.getImages().isEmpty()) {
                        List<String> urls = step.getImages().stream()
                            .map(StepImage::getImageUrl)
                            .collect(Collectors.toList());
                        stepMap.put("imageUrls", urls);
                    } else {
                        stepMap.put("imageUrls", java.util.List.of());
                    }
                    
                    treatmentSteps.add(stepMap);
                });
        }
        
        // Fallback: Nếu không có treatment plan, lấy từ medical record details
        if (treatmentSteps.isEmpty() && record.getDetails() != null && !record.getDetails().isEmpty()) {
            for (MedicalRecordDetail detail : record.getDetails()) {
                Map<String, Object> stepMap = new java.util.HashMap<>();
                stepMap.put("serviceName", detail.getService() != null ? detail.getService().getName() : "Dịch vụ");
                stepMap.put("toothNumber", detail.getToothNumber() != null ? detail.getToothNumber() : "");
                stepMap.put("notes", detail.getTreatmentNote() != null ? detail.getTreatmentNote() : "");
                stepMap.put("completedAt", record.getCreatedAt() != null ? 
                    record.getCreatedAt().format(dateFormatter) : "");
                stepMap.put("imageUrls", java.util.List.of());
                treatmentSteps.add(stepMap);
            }
        }
        
        item.put("treatmentSteps", treatmentSteps);
        
        // Get invoice info if exists
        if (record.getAppointment() != null) {
            Optional<Invoice> invoiceOpt = invoiceRepository.findByAppointmentId(record.getAppointment().getId());
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                item.put("totalAmount", String.format("%,.0f VNĐ", invoice.getTotalAmount()));
                item.put("paymentStatus", invoice.getPaymentStatus() != null ? 
                    invoice.getPaymentStatus().toString() : "N/A");
            } else {
                item.put("totalAmount", "N/A");
                item.put("paymentStatus", "N/A");
            }
        } else {
            item.put("totalAmount", "N/A");
            item.put("paymentStatus", "N/A");
        }
        
        return item;
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
