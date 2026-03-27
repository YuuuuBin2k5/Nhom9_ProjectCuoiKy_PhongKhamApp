package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.TreatmentPlanDTO;
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
    private final com.hcmute.clinic.repository.CheckInQueueRepository checkInQueueRepository;
    private final com.hcmute.clinic.service.QueueEventService queueEventService;
    private final com.hcmute.clinic.repository.NotificationRepository notificationRepository;
    private final com.hcmute.clinic.repository.DoctorRepository doctorRepository;

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
            return ResponseEntity.ok(toDTO(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private TreatmentPlanDTO toDTO(TreatmentPlan plan) {
        List<TreatmentPlanDTO.StepDTO> steps = plan.getSteps() != null ? plan.getSteps().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getSequenceOrder() != null ? a.getSequenceOrder() : 0,
                        b.getSequenceOrder() != null ? b.getSequenceOrder() : 0))
                .map(s -> TreatmentPlanDTO.StepDTO.builder()
                        .id(s.getId())
                        .treatmentPlanId(plan.getId())
                        .serviceId(s.getService() != null ? s.getService().getId() : 0L)
                        .serviceName(s.getService() != null ? s.getService().getName() : "")
                        .description(s.getService() != null ? s.getService().getDescription() : "")
                        .stepOrder(s.getSequenceOrder())
                        .status(s.getStatus().name())
                        .toothNumber(s.getToothNumber())
                        .estimatedPrice(s.getService() != null && s.getService().getPrice() != null ? s.getService().getPrice().doubleValue() : 0.0)
                        .actualPrice(s.getActualPrice() != null ? s.getActualPrice().doubleValue() : 0.0)
                        .doctorConclusion(s.getDoctorConclusion())
                        .roomName(s.getClinicRoom() != null ? s.getClinicRoom().getName() : null)
                        .uiTemplateType(s.getService() != null && s.getService().getUiTemplateType() != null ? s.getService().getUiTemplateType().name() : "GENERAL")
                        .build())
                .collect(Collectors.toList()) : List.of();

        return TreatmentPlanDTO.builder()
                .id(plan.getId())
                .patientId(plan.getPatient().getId())
                .status(plan.getStatus().name())
                .isDraft(plan.isDraft())
                .steps(steps)
                .build();
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
            return ResponseEntity.ok(toDTO(plan));
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

    @GetMapping("/{id}/for-room")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getByIdForRoom(@PathVariable Long id, Authentication auth) {
        try {
            TreatmentPlan plan = treatmentPlanService.getById(id);
            String email = auth.getName();
            
            // Tìm phòng của bác sĩ
            com.hcmute.clinic.entity.Doctor doc = doctorRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
            Long docRoomId = doc.getClinicRoom() != null ? doc.getClinicRoom().getId() : null;

            TreatmentPlanDTO dto = toDTO(plan);
            
            // Set editable flag per step
            if (dto.getSteps() != null) {
                for (TreatmentPlanDTO.StepDTO s : dto.getSteps()) {
                    boolean isEditable = false;
                    // Nếu bước PENDING hoặc IN_PROGRESS, VÀ bác sĩ đang ở cùng phòng với bước đó
                    if (("PENDING".equals(s.getStatus()) || "IN_PROGRESS".equals(s.getStatus())) 
                            && docRoomId != null 
                            && plan.getSteps().stream().anyMatch(st -> st.getId().equals(s.getId()) && st.getClinicRoom() != null && st.getClinicRoom().getId().equals(docRoomId))) {
                        isEditable = true;
                    }
                    s.setEditable(isEditable);
                }
            }

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/steps/{stepId}/start")
    public ResponseEntity<?> startStep(@PathVariable Long stepId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            treatmentPlanService.startStep(stepId);
            return ResponseEntity.ok(Map.of("message", "Đã bắt đầu bước điều trị"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> activatePlan(@PathVariable Long id) {
        try {
            treatmentPlanService.activatePlan(id);
            return ResponseEntity.ok(Map.of("message", "Phác đồ đã được kích hoạt"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/steps/{stepId}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> completeStep(@PathVariable Long stepId, @RequestBody(required = false) Map<String, String> body, Authentication auth) {
        try {
            // Check cross-room permission
            String email = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            Long docRoomId = null;
            if (!isAdmin) {
                com.hcmute.clinic.entity.Doctor doc = doctorRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                docRoomId = doc.getClinicRoom() != null ? doc.getClinicRoom().getId() : null;
            }
            
            String conclusion = body != null ? body.get("doctorConclusion") : null;
            String nextRoom = treatmentPlanService.completeStepAndAdvance(stepId, conclusion, docRoomId, checkInQueueRepository, queueEventService, notificationRepository);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Đã hoàn thành bước điều trị");
            if (nextRoom != null) {
                response.put("nextRoomName", nextRoom);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record PlanSummary(Long id, String title, String status, String createdAt, List<StepSummary> steps, int totalSteps, int completedSteps, String nextStepName) {
    }

    public record StepSummary(Long id, Integer order, String serviceName, String roomName, String status) {
    }
}
