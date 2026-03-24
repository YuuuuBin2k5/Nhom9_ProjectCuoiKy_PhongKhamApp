package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.UpdatePlanStepsRequest;
import com.hcmute.clinic.entity.TreatmentPlan;
import com.hcmute.clinic.entity.TreatmentPlanStep;
import com.hcmute.clinic.service.TreatmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/treatment-plans")
@RequiredArgsConstructor
public class TreatmentPlanController {

    private final TreatmentPlanService treatmentPlanService;

    @PostMapping("/from-template")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createFromTemplate(@RequestBody Map<String, Long> body, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        Long templateId = body.get("templateId");
        Long patientId = body.get("patientId");
        Long medicalRecordId = body.get("medicalRecordId");
        if (templateId == null || patientId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "templateId and patientId are required"));
        }
        try {
            TreatmentPlan plan = treatmentPlanService.createFromTemplate(templateId, patientId, medicalRecordId);
            return ResponseEntity.ok(Map.of("id", plan.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> myPlans(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        long patientId = Long.parseLong(auth.getName());
        List<TreatmentPlan> plans = treatmentPlanService.findByPatientId(patientId);
        if (plans == null) plans = List.of();
        List<PlanSummary> summaries = plans.stream()
                .map(p -> {
                    List<TreatmentPlanStep> steps = p.getSteps() != null ? p.getSteps().stream()
                            .sorted((a, b) -> Integer.compare(
                                    a.getSequenceOrder() != null ? a.getSequenceOrder() : 0,
                                    b.getSequenceOrder() != null ? b.getSequenceOrder() : 0))
                            .collect(Collectors.toList()) : List.of();

                    int total = steps.size();
                    int completed = (int) steps.stream().filter(s -> "COMPLETED".equals(s.getStatus().name())).count();
                    String nextStep = steps.stream()
                            .filter(s -> !"COMPLETED".equals(s.getStatus().name()) && !"CANCELLED".equals(s.getStatus().name()))
                            .findFirst()
                            .map(s -> s.getService() != null ? s.getService().getName() : "")
                            .orElse("");

                    return new PlanSummary(
                            p.getId(),
                            total > 0 ? steps.get(0).getService().getName() : "Phác đồ điều trị",
                            p.getStatus().name(),
                            p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                            steps.stream().map(s -> new StepSummary(
                                    s.getId(),
                                    s.getSequenceOrder(),
                                    s.getService() != null ? s.getService().getName() : "",
                                    s.getClinicRoom() != null ? s.getClinicRoom().getName() : null,
                                    s.getStatus().name()
                            )).collect(Collectors.toList()),
                            total,
                            completed,
                            nextStep
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            TreatmentPlan plan = treatmentPlanService.getById(id);
            List<Map<String, Object>> stepSummaries = plan.getSteps() != null ? plan.getSteps().stream()
                    .sorted((a, b) -> Integer.compare(a.getSequenceOrder() != null ? a.getSequenceOrder() : 0, b.getSequenceOrder() != null ? b.getSequenceOrder() : 0))
                    .map(s -> Map.<String, Object>of(
                            "id", s.getId(),
                            "sequenceOrder", s.getSequenceOrder() != null ? s.getSequenceOrder() : 0,
                            "serviceId", s.getService() != null ? s.getService().getId() : 0L,
                            "serviceName", s.getService() != null ? s.getService().getName() : "",
                            "clinicRoomId", s.getClinicRoom() != null ? s.getClinicRoom().getId() : 0L,
                            "roomName", s.getClinicRoom() != null ? s.getClinicRoom().getName() : "",
                            "status", s.getStatus().name(),
                            "toothNumber", s.getToothNumber() != null ? s.getToothNumber() : "",
                            "doctorConclusion", s.getDoctorConclusion() != null ? s.getDoctorConclusion() : "",
                            "uiTemplateType", s.getService() != null && s.getService().getUiTemplateType() != null ? s.getService().getUiTemplateType().name() : "GENERAL"
                    ))
                    .collect(Collectors.toList()) : List.of();
            return ResponseEntity.ok(Map.of(
                    "id", plan.getId(),
                    "patientId", plan.getPatient().getId(),
                    "status", plan.getStatus().name(),
                    "steps", stepSummaries
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSteps(@PathVariable Long id, @RequestBody UpdatePlanStepsRequest request) {
        try {
            treatmentPlanService.updateSteps(id, request);
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật phác đồ"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/steps/{stepId}/start")
    public ResponseEntity<?> startStep(@PathVariable Long stepId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            treatmentPlanService.updateStepStatus(stepId, "IN_PROGRESS");
            return ResponseEntity.ok(Map.of("message", "Đã bắt đầu bước điều trị"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record PlanSummary(Long id, String title, String status, String createdAt, List<StepSummary> steps, int totalSteps, int completedSteps, String nextStepName) {
    }

    public record StepSummary(Long id, Integer order, String serviceName, String roomName, String status) {
    }
}
