package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.PrescriptionDTO;
import com.hcmute.clinic.dto.PrescriptionRequest;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.DoctorRepository;
import com.hcmute.clinic.repository.MedicalRecordRepository;
import com.hcmute.clinic.repository.PrescriptionRepository;
import com.hcmute.clinic.repository.TreatmentPlanStepRepository;
import com.hcmute.clinic.repository.TreatmentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanStepRepository treatmentPlanStepRepository;

    @Transactional
    public PrescriptionDTO createPrescription(PrescriptionRequest request, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmailIgnoreCase(doctorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bác sĩ không tồn tại"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch hẹn"));

        // Get or Create MedicalRecord
        MedicalRecord medicalRecord = medicalRecordRepository.findByAppointmentId(appointment.getId())
                .orElseGet(() -> {
                    MedicalRecord newRecord = MedicalRecord.builder()
                            .appointment(appointment)
                            .patient(appointment.getPatient())
                            .doctor(doctor)
                            .diagnosis(request.getDiagnosis())
                            .symptoms(request.getSymptoms())
                            .advice(request.getAdvice())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return medicalRecordRepository.save(newRecord);
                });

        // Update MedicalRecord details if provided
        if (request.getDiagnosis() != null) medicalRecord.setDiagnosis(request.getDiagnosis());
        if (request.getSymptoms() != null) medicalRecord.setSymptoms(request.getSymptoms());
        if (request.getAdvice() != null) medicalRecord.setAdvice(request.getAdvice());
        
        // Data Freezing: Check if treatment plan is completed
        java.util.Optional<TreatmentPlan> planOpt = treatmentPlanRepository.findFirstByMedicalRecordId(medicalRecord.getId());
        if (planOpt.isPresent() && "COMPLETED".equalsIgnoreCase(planOpt.get().getStatus().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Hồ sơ đã hoàn tất và đóng dữ liệu. Không thể kê thêm đơn thuốc.");
        }

        medicalRecordRepository.save(medicalRecord);

        // Check if Prescription already exists for this MedicalRecord
        Prescription prescription = medicalRecord.getPrescription();
        boolean isStepScoped = request.getTreatmentPlanStepId() != null;
        Long requestStepId = request.getTreatmentPlanStepId();

        if (prescription == null) {
            prescription = Prescription.builder()
                    .medicalRecord(medicalRecord)
                    .doctor(doctor)
                    .createdAt(LocalDateTime.now())
                    .details(new ArrayList<>())
                    .build();
        } else {
            // Replace details for step only (kê đơn theo từng dịch vụ/step)
            if (prescription.getDetails() == null) {
                prescription.setDetails(new ArrayList<>());
            }

            if (!isStepScoped) {
                // Legacy behaviour: replace entire prescription
                prescription.getDetails().clear();
            } else {
                // Remove existing details for the same step to avoid duplicating medicines
                // Also remove legacy rows with null treatmentPlanStepId (previous version)
                prescription.getDetails().removeIf(d ->
                        d.getTreatmentPlanStepId() == null
                                || Objects.equals(d.getTreatmentPlanStepId(), requestStepId));
            }
        }

        // Add details
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (PrescriptionRequest.DetailRequest d : request.getDetails()) {
                PrescriptionDetail detail = PrescriptionDetail.builder()
                        .prescription(prescription)
                        .treatmentPlanStepId(requestStepId)
                        .medicineName(d.getMedicineName())
                        .dosage(d.getDosage())
                        .frequency(d.getFrequency())
                        .duration(d.getDuration())
                        .unit(d.getUnit())
                        .build();
                prescription.getDetails().add(detail);
            }
        }

        prescription = prescriptionRepository.save(prescription);
        medicalRecord.setPrescription(prescription); // Maintain bidirectional setup
        medicalRecordRepository.save(medicalRecord);

        // Update billing price for step (actualPrice) from entered amount
        if (requestStepId != null) {
            TreatmentPlanStep step = treatmentPlanStepRepository.findById(requestStepId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy step/dịch vụ trong phác đồ"));

            // Safety: đảm bảo step thuộc đúng appointment đang kê
            if (step.getPlan() == null
                    || step.getPlan().getAppointment() == null
                    || step.getPlan().getAppointment().getId() == null
                    || !step.getPlan().getAppointment().getId().equals(appointment.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Step/dịch vụ không thuộc lịch hẹn này");
            }

            BigDecimal basePrice = (step.getService() != null && step.getService().getPrice() != null)
                    ? step.getService().getPrice()
                    : BigDecimal.ZERO;
            BigDecimal extra = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;

            // "amount" là phần tiền bác sĩ nhập cần cộng thêm vào giá dịch vụ
            step.setActualPrice(basePrice.add(extra));
            treatmentPlanStepRepository.save(step);
        }

        return mapToDTO(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ y tế cho lịch hẹn này"));

        Prescription prescription = record.getPrescription();
        if (prescription == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chưa có đơn thuốc cho hồ sơ y tế này");
        }

        return mapToDTO(prescription);
    }

    private PrescriptionDTO mapToDTO(Prescription prescription) {
        List<PrescriptionDTO.DetailDTO> details = prescription.getDetails().stream()
                .map(d -> PrescriptionDTO.DetailDTO.builder()
                        .id(d.getId())
                        .treatmentPlanStepId(d.getTreatmentPlanStepId())
                        .medicineName(d.getMedicineName())
                        .dosage(d.getDosage())
                        .frequency(d.getFrequency())
                        .duration(d.getDuration())
                        .unit(d.getUnit())
                        .build())
                .collect(Collectors.toList());

        return PrescriptionDTO.builder()
                .id(prescription.getId())
                .medicalRecordId(prescription.getMedicalRecord().getId())
                .doctorId(prescription.getDoctor().getId())
                .doctorName(prescription.getDoctor().getLastName() + " " + prescription.getDoctor().getFirstName())
                .diagnosis(prescription.getMedicalRecord() != null ? prescription.getMedicalRecord().getDiagnosis() : null)
                .symptoms(prescription.getMedicalRecord() != null ? prescription.getMedicalRecord().getSymptoms() : null)
                .advice(prescription.getMedicalRecord() != null ? prescription.getMedicalRecord().getAdvice() : null)
                .createdAt(prescription.getCreatedAt())
                .details(details)
                .build();
    }
}
