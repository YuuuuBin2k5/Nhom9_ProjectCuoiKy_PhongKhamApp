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
    private final com.hcmute.clinic.repository.StepImageRepository stepImageRepository;
    private final com.hcmute.clinic.service.InvoiceService invoiceService;

    @PostMapping("/from-template")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createFromTemplate(@RequestBody Map<String, Long> body, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        Long templateId = body.get("templateId");
        Long patientId = body.get("patientId");
        Long medicalRecordId = body.get("medicalRecordId");
        if (patientId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "patientId is required"));
        }
        try {
            TreatmentPlan plan = treatmentPlanService.createFromTemplate(templateId, patientId, medicalRecordId);
            return ResponseEntity.ok(toDTO(plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/from-appointment")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createFromAppointment(@RequestBody Map<String, Long> body, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        Long appointmentId = body.get("appointmentId");
        Long templateId = body.get("templateId");
        if (appointmentId == null || templateId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "appointmentId and templateId are required"));
        }
        try {
            TreatmentPlan plan = treatmentPlanService.createFromAppointment(appointmentId, templateId);
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

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getByPatient(@PathVariable Long patientId) {
        try {
            List<TreatmentPlan> plans = treatmentPlanService.findByPatientId(patientId);
            List<TreatmentPlanDTO> dtos = plans.stream().map(this::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
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
            TreatmentPlan plan = treatmentPlanService.getById(id);
            
            // Allow updates if plan is COMPLETED but has IN_PROGRESS steps (parallel workflow)
            boolean hasInProgressSteps = plan.getSteps() != null && plan.getSteps().stream()
                    .anyMatch(s -> s.getStatus() == com.hcmute.clinic.enums.StepStatus.IN_PROGRESS);
            
            if (plan.getStatus() == com.hcmute.clinic.enums.TreatmentPlanStatus.COMPLETED && !hasInProgressSteps) {
                return ResponseEntity.badRequest().body(Map.of("message", "Hồ sơ đã hoàn tất và bị khóa, không thể cập nhật"));
            }
            
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
            String authName = auth.getName();
            com.hcmute.clinic.entity.Doctor doc = null;
            try {
                Long docId = Long.parseLong(authName);
                doc = doctorRepository.findById(docId).orElse(null);
            } catch (Exception e) {}

            if (doc == null) {
                doc = doctorRepository.findByEmailIgnoreCase(authName)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
            }
            Long docRoomId = doc.getClinicRoom() != null ? doc.getClinicRoom().getId() : null;

            TreatmentPlanDTO dto = toDTO(plan);
            
            // Fetch original room to identify the main doctor
            Long originalRoomId = null;
            java.util.List<com.hcmute.clinic.entity.CheckInQueue> queues = checkInQueueRepository.findTodayForPatient(
                plan.getPatient().getId(), 
                java.time.LocalDate.now().atStartOfDay(), 
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
            );
            if (!queues.isEmpty()) {
                originalRoomId = queues.get(0).getOriginalRoomId();
                if (originalRoomId == null) originalRoomId = queues.get(0).getClinicRoom().getId();
            }

            // Set editable flag per step
            if (dto.getSteps() != null) {
                for (TreatmentPlanDTO.StepDTO s : dto.getSteps()) {
                    boolean isEditable = false;
                    
                    // Case 1: Doctor is the main doctor (original room) -> Can edit everything
                    if (docRoomId != null && docRoomId.equals(originalRoomId)) {
                        // Allow editing all steps except CANCELLED
                        if (!"CANCELLED".equals(s.getStatus())) {
                            isEditable = true;
                        }
                    } 
                    // Case 2: Room doctor -> Can edit steps in their room
                    else if (docRoomId != null) {
                        Long stepRoomId = plan.getSteps().stream()
                                .filter(st -> st.getId().equals(s.getId()))
                                .findFirst()
                                .map(st -> st.getClinicRoom() != null ? st.getClinicRoom().getId() : null)
                                .orElse(null);
                        
                        if (docRoomId.equals(stepRoomId) || stepRoomId == null) {
                            // Allow editing all steps except CANCELLED
                            if (!"CANCELLED".equals(s.getStatus())) {
                                isEditable = true;
                            }
                        }
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

    @PatchMapping("/steps/{stepId}/cancel")
    public ResponseEntity<?> cancelStep(@PathVariable Long stepId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            treatmentPlanService.cancelStep(stepId);
            return ResponseEntity.ok(Map.of("message", "Đã hủy bước điều trị"));
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
    public ResponseEntity<?> completeStep(@PathVariable Long stepId, @RequestBody(required = false) Map<String, Object> body, Authentication auth) {
        try {
            // Check cross-room permission
            String authName = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            Long docRoomId = null;
            if (!isAdmin) {
                com.hcmute.clinic.entity.Doctor doc = null;
                try {
                    Long docId = Long.parseLong(authName);
                    doc = doctorRepository.findById(docId).orElse(null);
                } catch (Exception e) {}

                if (doc == null) {
                    doc = doctorRepository.findByEmailIgnoreCase(authName)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                }
                docRoomId = doc.getClinicRoom() != null ? doc.getClinicRoom().getId() : null;
            }
            
            String conclusion = body != null ? (String) body.get("doctorConclusion") : null;
            List<String> imageUrls = body != null ? (List<String>) body.get("imageUrls") : null;

            String nextRoom = treatmentPlanService.completeStepAndAdvance(stepId, conclusion, imageUrls, docRoomId, checkInQueueRepository, queueEventService, notificationRepository);
            
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
    
    @PostMapping("/{planId}/complete-and-generate-invoice")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public ResponseEntity<?> completeAndGenerateInvoice(@PathVariable Long planId, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            com.hcmute.clinic.entity.Invoice invoice = invoiceService.createInvoiceFromTreatmentPlan(planId);
            
            // Convert to DTO
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", invoice.getId());
            response.put("patientId", invoice.getPatient().getId());
            response.put("patientName", invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName());
            response.put("treatmentPlanId", invoice.getTreatmentPlan() != null ? invoice.getTreatmentPlan().getId() : null);
            response.put("totalAmount", invoice.getTotalAmount().doubleValue());
            response.put("paymentStatus", invoice.getPaymentStatus().name());
            response.put("createdAt", invoice.getCreatedAt().toString());
            
            // Add items
            if (invoice.getItems() != null) {
                List<Map<String, Object>> items = invoice.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new java.util.HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("serviceName", item.getServiceName());
                        itemMap.put("toothNumber", item.getToothNumber());
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("unitPrice", item.getUnitPrice().doubleValue());
                        itemMap.put("totalPrice", item.getTotalPrice().doubleValue());
                        itemMap.put("description", item.getDescription());
                        return itemMap;
                    })
                    .collect(Collectors.toList());
                response.put("items", items);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/steps/{stepId}/images")
    public ResponseEntity<?> getStepImages(@PathVariable Long stepId) {
        try {
            List<com.hcmute.clinic.entity.StepImage> images = stepImageRepository.findByStepIdOrderByCreatedAtDesc(stepId);
            List<Map<String, Object>> result = images.stream()
                .map(img -> Map.of(
                    "id", (Object) img.getId(),
                    "imageUrl", img.getImageUrl(),
                    "description", img.getDescription() != null ? img.getDescription() : "",
                    "createdAt", img.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public record PlanSummary(Long id, String title, String status, String createdAt, List<StepSummary> steps, int totalSteps, int completedSteps, String nextStepName) {
    }

    public record StepSummary(Long id, Integer order, String serviceName, String roomName, String status) {
    }
}
