package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.UpdatePlanStepsRequest;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.enums.TreatmentPlanStatus;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentPlanService {

    private final TreatmentPlanTemplateRepository templateRepository;
    private final TreatmentPlanRepository planRepository;
    private final TreatmentPlanStepRepository stepRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceRepository serviceRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final NotificationRepository notificationRepository;

    public List<TreatmentPlanTemplate> listActiveTemplates() {
        return templateRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public TreatmentPlan createFromTemplate(Long templateId, Long patientId, Long medicalRecordId) {
        TreatmentPlanTemplate template = templateRepository.findByIdWithSteps(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mẫu không tồn tại"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bệnh nhân không tồn tại"));

        MedicalRecord medicalRecord = null;
        if (medicalRecordId != null) {
            medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElse(null);
        }

        TreatmentPlan plan = TreatmentPlan.builder()
                .patient(patient)
                .medicalRecord(medicalRecord)
                .templateId(template.getId())
                .status(TreatmentPlanStatus.IN_PROGRESS)
                .build();
        plan = planRepository.save(plan);
        if (plan.getSteps() == null) {
            plan.setSteps(new ArrayList<>());
        }

        List<TreatmentPlanTemplateStep> templateSteps = template.getSteps();
        if (templateSteps != null && !templateSteps.isEmpty()) {
            List<TreatmentPlanTemplateStep> sorted = new ArrayList<>(templateSteps);
            sorted.sort(Comparator.comparingInt(TreatmentPlanTemplateStep::getSequenceOrder));

            for (TreatmentPlanTemplateStep ts : sorted) {
                TreatmentPlanStep step = TreatmentPlanStep.builder()
                        .plan(plan)
                        .service(ts.getService())
                        .clinicRoom(ts.getClinicRoom())
                        .sequenceOrder(ts.getSequenceOrder())
                        .status(StepStatus.PENDING)
                        .build();
                plan.getSteps().add(step);
            }
            planRepository.save(plan);
        }
        Notification notif = Notification.builder()
                .patient(patient)
                .title("Phác đồ điều trị mới")
                .message("Bác sĩ đã lập phác đồ điều trị cho bạn. Vui lòng xem chi tiết trong mục Phác đồ.")
                .type("TREATMENT_PLAN")
                .build();
        notificationRepository.save(notif);
        return plan;
    }

    @Transactional
    public TreatmentPlan updateSteps(Long planId, UpdatePlanStepsRequest request) {
        TreatmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ không tồn tại"));
        if (request == null || request.getSteps() == null || request.getSteps().isEmpty()) {
            plan.getSteps().clear();
            return planRepository.save(plan);
        }
        plan.getSteps().clear();
        int order = 0;
        for (UpdatePlanStepsRequest.StepItem item : request.getSteps()) {
            com.hcmute.clinic.entity.Service svc = serviceRepository.findById(item.getServiceId() != null ? item.getServiceId() : 0L)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dịch vụ không tồn tại: " + item.getServiceId()));
            ClinicRoom room = null;
            if (item.getClinicRoomId() != null) {
                room = clinicRoomRepository.findById(item.getClinicRoomId()).orElse(null);
            }
            int seq = item.getSequenceOrder() != null ? item.getSequenceOrder() : order;
            TreatmentPlanStep step = TreatmentPlanStep.builder()
                    .plan(plan)
                    .service(svc)
                    .clinicRoom(room)
                    .sequenceOrder(seq)
                    .status(item.getStatus() != null ? StepStatus.valueOf(item.getStatus().toUpperCase()) : StepStatus.PENDING)
                    .toothNumber(item.getToothNumber())
                    .doctorConclusion(item.getDoctorConclusion())
                    .build();
            plan.getSteps().add(step);
            order++;
        }
        return planRepository.save(plan);
    }

    public TreatmentPlan getById(Long id) {
        return planRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ không tồn tại"));
    }

    public List<TreatmentPlan> findByPatientId(Long patientId) {
        return planRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional
    public void updateStepStatus(Long stepId, String status) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
        try {
            step.setStatus(StepStatus.valueOf(status.toUpperCase()));
            stepRepository.save(step);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + status);
        }
    }
}
