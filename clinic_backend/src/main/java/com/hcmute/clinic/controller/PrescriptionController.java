package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.PrescriptionDTO;
import com.hcmute.clinic.dto.PrescriptionRequest;
import com.hcmute.clinic.entity.Doctor;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.entity.Prescription;
import com.hcmute.clinic.entity.PrescriptionDetail;
import com.hcmute.clinic.repository.PrescriptionRepository;
import com.hcmute.clinic.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionRepository prescriptionRepository;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionDTO> createOrUpdatePrescription(
            @RequestBody PrescriptionRequest request,
            Authentication authentication) {
        String doctorIdentifier = authentication.getName(); // Can be ID or email
        PrescriptionDTO dto = prescriptionService.createPrescription(request, doctorIdentifier);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<PrescriptionDTO> getPrescriptionByAppointment(@PathVariable Long appointmentId) {
        PrescriptionDTO dto = prescriptionService.getPrescriptionByAppointmentId(appointmentId);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        
        // Generate simple text-based prescription document
        StringBuilder content = new StringBuilder();
        content.append("=".repeat(80)).append("\n");
        content.append("                          ĐƠN THUỐC / PRESCRIPTION\n");
        content.append("=".repeat(80)).append("\n\n");
        
        // Patient information
        content.append("THÔNG TIN BỆNH NHÂN / PATIENT INFORMATION\n");
        content.append("-".repeat(80)).append("\n");
        if (prescription.getMedicalRecord() != null && prescription.getMedicalRecord().getPatient() != null) {
            Patient patient = prescription.getMedicalRecord().getPatient();
            content.append("Họ tên / Name: ")
                   .append(patient.getFirstName() + " " + patient.getLastName())
                   .append("\n");
            content.append("Ngày sinh / DOB: ")
                   .append(patient.getDob() != null ? patient.getDob().toString() : "N/A")
                   .append("\n");
            content.append("Số điện thoại / Phone: ")
                   .append(patient.getPhone() != null ? patient.getPhone() : "N/A")
                   .append("\n");
        }
        content.append("\n");
        
        // Doctor information
        content.append("THÔNG TIN BÁC SĨ / DOCTOR INFORMATION\n");
        content.append("-".repeat(80)).append("\n");
        if (prescription.getDoctor() != null) {
            Doctor doctor = prescription.getDoctor();
            content.append("Bác sĩ / Doctor: ")
                   .append(doctor.getFirstName() + " " + doctor.getLastName())
                   .append("\n");
            content.append("Chuyên khoa / Specialty: ")
                   .append(doctor.getSpecialization() != null ? doctor.getSpecialization() : "N/A")
                   .append("\n");
        }
        content.append("Ngày kê đơn / Date: ")
               .append(prescription.getCreatedAt() != null ? 
                      prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A")
               .append("\n\n");
        
        // Prescription details
        content.append("CHI TIẾT ĐƠN THUỐC / PRESCRIPTION DETAILS\n");
        content.append("-".repeat(80)).append("\n");
        content.append(String.format("%-4s %-30s %-15s %-15s %-10s\n", 
                      "STT", "Tên thuốc", "Liều lượng", "Tần suất", "Thời gian"));
        content.append(String.format("%-4s %-30s %-15s %-15s %-10s\n", 
                      "No.", "Medicine", "Dosage", "Frequency", "Duration"));
        content.append("-".repeat(80)).append("\n");
        
        int index = 1;
        if (prescription.getDetails() != null) {
            for (PrescriptionDetail detail : prescription.getDetails()) {
                content.append(String.format("%-4d %-30s %-15s %-15s %-10s\n",
                              index++,
                              detail.getMedicineName() != null ? detail.getMedicineName() : "",
                              detail.getDosage() != null ? detail.getDosage() : "",
                              detail.getFrequency() != null ? detail.getFrequency() : "",
                              detail.getDuration() != null ? detail.getDuration() : ""));
            }
        }
        content.append("\n");
        
        // Footer
        content.append("\n\n");
        content.append(" ".repeat(50)).append("Chữ ký bác sĩ\n");
        content.append(" ".repeat(50)).append("Doctor's Signature\n");
        content.append(" ".repeat(50)).append("_________________\n\n");
        
        content.append("=".repeat(80)).append("\n");
        content.append("Lưu ý: Đơn thuốc này chỉ có giá trị trong 30 ngày kể từ ngày kê đơn.\n");
        content.append("Note: This prescription is valid for 30 days from the date of issue.\n");
        content.append("=".repeat(80)).append("\n");
        
        byte[] pdfContent = content.toString().getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "prescription_" + id + ".txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
}
