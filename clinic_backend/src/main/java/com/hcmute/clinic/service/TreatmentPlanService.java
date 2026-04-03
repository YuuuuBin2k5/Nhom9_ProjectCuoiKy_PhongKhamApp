package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.TreatmentPlanTemplateRequest;
import com.hcmute.clinic.dto.TreatmentPlanTemplateResponseDTO;
import com.hcmute.clinic.dto.UpdatePlanStepsRequest;
import com.hcmute.clinic.entity.*;
import com.hcmute.clinic.enums.StepStatus;
import com.hcmute.clinic.enums.TreatmentPlanStatus;
import com.hcmute.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TreatmentPlanService - Dịch vụ lõi quản lý Phác đồ điều trị.
 * Chịu trách nhiệm thực thi các flow:
 * - SE_13: Lập phác đồ điều trị.
 * - SE_14: Ghi nhận kết quả điều trị.
 * - SE_15: Thanh toán và hoàn tất phác đồ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanService {

    private final TreatmentPlanTemplateRepository templateRepository;
    private final TreatmentPlanRepository planRepository;
    private final TreatmentPlanStepRepository stepRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceRepository serviceRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final ServiceRoomAssignmentService roomAssignmentService;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    private final com.hcmute.clinic.repository.AppointmentRepository appointmentRepository;
    private final com.hcmute.clinic.repository.InvoiceRepository invoiceRepository;
    private final com.hcmute.clinic.repository.CheckInQueueRepository checkInQueueRepository;
    private final QueueEventService queueEventService;

    public List<TreatmentPlanTemplateResponseDTO> listActiveTemplates() {
        return templateRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(this::convertToResponseDTO).collect(java.util.stream.Collectors.toList());
    }

    public List<TreatmentPlanTemplateResponseDTO> listAllTemplates() {
        return templateRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
                .stream().map(this::convertToResponseDTO).collect(java.util.stream.Collectors.toList());
    }

    public TreatmentPlanTemplateResponseDTO getTemplateById(Long id) {
        TreatmentPlanTemplate template = templateRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mẫu không tồn tại"));
        return convertToResponseDTO(template);
    }

    /**
     * FIX 5: Tạo TreatmentPlan từ Appointment
     * Đây là method mới để tạo plan đúng cách từ appointment
     */
    @Transactional
    public TreatmentPlan createFromAppointment(Long appointmentId, Long templateId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch hẹn"));
        
        // Kiểm tra xem đã có plan chưa
        Optional<TreatmentPlan> existingPlan = planRepository
                .findFirstByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        if (existingPlan.isPresent() && existingPlan.get().getStatus() != TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Lịch hẹn này đã có phác đồ điều trị đang hoạt động");
        }
        
        // Tạo hoặc lấy MedicalRecord
        MedicalRecord medicalRecord = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> {
                    MedicalRecord newRecord = MedicalRecord.builder()
                            .appointment(appointment)
                            .patient(appointment.getPatient())
                            .doctor(appointment.getDoctor())
                            .createdAt(java.time.LocalDateTime.now())
                            .build();
                    return medicalRecordRepository.save(newRecord);
                });
        
        // Tạo TreatmentPlan
        return createFromTemplate(templateId, appointment.getPatient().getId(), medicalRecord.getId());
    }

    /**
     * SE_13: Lập phác đồ điều trị từ mẫu (Template).
     * Khởi tạo các bước điều trị mặc định dựa trên dịch vụ đã chọn.
     */
    @Transactional
    public TreatmentPlan createFromTemplate(Long templateId, Long patientId, Long medicalRecordId) {
        TreatmentPlanTemplate template = null;
        if (templateId != null) {
            template = templateRepository.findByIdWithSteps(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mẫu không tồn tại"));
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bệnh nhân không tồn tại"));

        MedicalRecord medicalRecord = null;
        if (medicalRecordId != null) {
            medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElse(null);
        }

        // FIX 2: Tìm appointment để link
        Appointment appointment = null;
        if (medicalRecord != null && medicalRecord.getAppointment() != null) {
            appointment = medicalRecord.getAppointment();
        }

        TreatmentPlan plan = TreatmentPlan.builder()
                .patient(patient)
                .medicalRecord(medicalRecord)
                .appointment(appointment)
                .templateId(template != null ? template.getId() : null)
                .status(TreatmentPlanStatus.IN_PROGRESS)
                .isDraft(false)  // CHANGED: Auto-activate plans (no draft mode)
                .build();
        plan = planRepository.save(plan);
        if (plan.getSteps() == null) {
            plan.setSteps(new java.util.LinkedHashSet<>());
        }

        if (template != null) {
            java.util.Set<TreatmentPlanTemplateStep> templateSteps = template.getSteps();
        if (templateSteps != null && !templateSteps.isEmpty()) {
            List<TreatmentPlanTemplateStep> sorted = new ArrayList<>(templateSteps);
            sorted.sort(Comparator.comparingInt(TreatmentPlanTemplateStep::getSequenceOrder));

            for (TreatmentPlanTemplateStep ts : sorted) {
                // CHANGED: Do NOT auto-assign room when creating from template
                // Room will be assigned when the step is started (when doctor clicks "Bắt đầu")
                // Only use room from template if explicitly set
                ClinicRoom assignedRoom = ts.getClinicRoom();
                
                TreatmentPlanStep step = TreatmentPlanStep.builder()
                        .plan(plan)
                        .service(ts.getService())
                        .clinicRoom(assignedRoom)  // CHANGED: Only use template room, don't auto-assign
                        .sequenceOrder(ts.getSequenceOrder())
                        .status(StepStatus.PENDING)
                        .medicationDetails(ts.getMedicationDetails())
                        .build();
                plan.getSteps().add(step);
            }
            planRepository.save(plan);
        }
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

    /**
     * Cập nhật danh sách các bước trong phác đồ.
     * Cho phép thêm mới, chỉnh sửa hoặc xóa các bước ở trạng thái PENDING.
     */
    @Transactional
    public TreatmentPlan updateSteps(Long planId, UpdatePlanStepsRequest request) {
        TreatmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phác đồ không tồn tại"));

        // Allow updates if plan is COMPLETED but has IN_PROGRESS steps (parallel workflow)
        // OR if adding new steps (doctor can add more services after completing initial steps)
        boolean hasInProgressSteps = plan.getSteps() != null && plan.getSteps().stream()
                .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
        
        boolean isAddingNewSteps = request != null && request.getSteps() != null && 
                request.getSteps().stream().anyMatch(s -> s.getId() == null);
        
        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && !hasInProgressSteps && !isAddingNewSteps) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa, không thể chỉnh sửa");
        }

        if (request == null || request.getSteps() == null) {
            return plan;
        }

        java.util.Set<TreatmentPlanStep> existingSteps = plan.getSteps();
        List<Long> requestedIds = request.getSteps().stream()
                .filter(s -> s.getId() != null)
                .map(UpdatePlanStepsRequest.StepItem::getId)
                .toList();

        // Remove steps not in the request, but ONLY if they are PENDING
        existingSteps.removeIf(step -> step.getId() != null 
                && !requestedIds.contains(step.getId()) 
                && step.getStatus() == StepStatus.PENDING);

        int order = 0;
        for (UpdatePlanStepsRequest.StepItem item : request.getSteps()) {
            int seq = item.getSequenceOrder() != null ? item.getSequenceOrder() : order;
            
            if (item.getId() != null) {
                // Update existing
                existingSteps.stream()
                        .filter(s -> s.getId().equals(item.getId()))
                        .findFirst()
                        .ifPresent(step -> {
                            step.setSequenceOrder(seq);
                            step.setToothNumber(item.getToothNumber());
                            step.setDoctorConclusion(item.getDoctorConclusion());
                            if (item.getStatus() != null) {
                                try {
                                    step.setStatus(StepStatus.valueOf(item.getStatus().toUpperCase()));
                                } catch (IllegalArgumentException ignored) {}
                            }
                            
                            // Update images if provided
                            if (item.getImageUrls() != null) {
                                if (step.getImages() == null) step.setImages(new java.util.LinkedHashSet<>());
                                // Simple approach: clear and re-add for draft updates
                                step.getImages().clear();
                                for (String url : item.getImageUrls()) {
                                    step.getImages().add(StepImage.builder().step(step).imageUrl(url).build());
                                }
                            }
                        });
            } else {
                // Create new - BUT FIRST: check if a PENDING step for this service/tooth already exists
                // to avoid duplicates from rapid sync calls
                TreatmentPlanStep existingPending = existingSteps.stream()
                        .filter(s -> s.getId() != null 
                                && s.getStatus() == StepStatus.PENDING 
                                && s.getService().getId().equals(item.getServiceId())
                                && ((s.getToothNumber() == null && item.getToothNumber() == null) || 
                                    (s.getToothNumber() != null && s.getToothNumber().equals(item.getToothNumber()))))
                        .findFirst()
                        .orElse(null);
                
                if (existingPending != null) {
                    existingPending.setSequenceOrder(seq);
                    existingPending.setDoctorConclusion(item.getDoctorConclusion());
                    continue;
                }

                // Actually create new
                com.hcmute.clinic.entity.Service svc = serviceRepository.findById(item.getServiceId() != null ? item.getServiceId() : 0L)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dịch vụ không tồn tại: " + item.getServiceId()));
                
                // Auto-assign clinic room based on service if not provided
                ClinicRoom room = null;
                if (item.getClinicRoomId() != null) {
                    room = clinicRoomRepository.findById(item.getClinicRoomId()).orElse(null);
                } else {
                    // REFACTORED: Use centralized room assignment service
                    room = roomAssignmentService.determineRoomForService(svc);
                    if (room != null) {
                        log.info("Auto-assigned room: {}", roomAssignmentService.explainRoomAssignment(svc, room));
                    }
                }
                
                TreatmentPlanStep step = TreatmentPlanStep.builder()
                        .plan(plan)
                        .service(svc)
                        .clinicRoom(room)
                        .sequenceOrder(seq)
                        .status(item.getStatus() != null ? StepStatus.valueOf(item.getStatus().toUpperCase()) : StepStatus.PENDING)
                        .toothNumber(item.getToothNumber())
                        .doctorConclusion(item.getDoctorConclusion())
                        .build();
                
                if (item.getImageUrls() != null) {
                    step.setImages(new java.util.LinkedHashSet<>());
                    for (String url : item.getImageUrls()) {
                        step.getImages().add(StepImage.builder().step(step).imageUrl(url).build());
                    }
                }
                
                existingSteps.add(step);
            }
            order++;
        }
        
        // If plan was COMPLETED but we added new PENDING steps, reopen it to IN_PROGRESS
        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED && isAddingNewSteps) {
            boolean hasNewPendingSteps = existingSteps.stream()
                    .anyMatch(s -> s.getStatus() == StepStatus.PENDING);
            if (hasNewPendingSteps) {
                plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);
            }
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

    public List<TreatmentPlan> findByAppointmentId(Long appointmentId) {
        return planRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }

    @Transactional
    public void updateStepStatus(Long stepId, String status) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
        
        if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        try {
            step.setStatus(StepStatus.valueOf(status.toUpperCase()));
            stepRepository.save(step);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + status);
        }
    }
    @Transactional
    public void startStep(Long stepId, Long doctorRoomId) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
        
        if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        if (step.getStatus() != StepStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bước này không ở trạng thái chờ");
        }
        
        // CHANGED: Assign room when starting step (if not already assigned)
        if (step.getClinicRoom() == null && step.getService() != null) {
            ClinicRoom assignedRoom = roomAssignmentService.determineRoomForService(step.getService());
            step.setClinicRoom(assignedRoom);
            log.info("Room assigned when starting step: {}", 
                roomAssignmentService.explainRoomAssignment(step.getService(), assignedRoom));
        }
        
        // VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa
        TreatmentPlan plan = step.getPlan();
        boolean hasPreviousIncomplete = plan.getSteps().stream()
                .filter(s -> s.getSequenceOrder() != null && step.getSequenceOrder() != null)
                .filter(s -> s.getSequenceOrder() < step.getSequenceOrder())
                .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
        
        if (hasPreviousIncomplete) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Không thể bắt đầu bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
        }
        
        // NEW: Transfer patient to new room when starting step (if room is different)
        ClinicRoom stepRoom = step.getClinicRoom();
        if (stepRoom != null) {
            transferPatientToRoom(plan, stepRoom);
        }
        
        // Kiểm tra quyền: Bác sĩ phải ở đúng phòng (nếu step có chỉ định phòng)
        // NOTE: Check this AFTER room transfer, so doctor can start step in the new room
        if (doctorRoomId != null && step.getClinicRoom() != null) {
            if (!doctorRoomId.equals(step.getClinicRoom().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền bắt đầu bước này. Bước này thuộc về phòng khác.");
            }
        }
        
        step.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(step);
    }
    
    /**
     * Transfer patient to a new room in the queue
     * @param plan Treatment plan
     * @param newRoom New clinic room
     */
    private void transferPatientToRoom(TreatmentPlan plan, ClinicRoom newRoom) {
        if (plan == null || plan.getPatient() == null || newRoom == null) {
            return;
        }
        
        // Find active queue for patient (including PAUSED_FOR_TEST and RETURNED_PRIORITY)
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<com.hcmute.clinic.entity.CheckInQueue> queues = 
            checkInQueueRepository.findTodayForPatient(
                plan.getPatient().getId(), 
                today.atStartOfDay(), 
                today.plusDays(1).atStartOfDay()
            );
            
        com.hcmute.clinic.entity.CheckInQueue activeQueue = queues.stream()
            .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.IN_PROGRESS 
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.PAUSED_FOR_TEST
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY)
            .findFirst()
            .orElse(null);
        
        if (activeQueue == null) {
            log.info("No active queue found for patient {}, skipping room transfer", plan.getPatient().getId());
            return;
        }
        
        // CRITICAL FIX: Do NOT transfer if patient is PAUSED_FOR_TEST (at X-Ray)
        // They will return to original room after X-Ray is complete
        if (activeQueue.getStatus() == com.hcmute.clinic.enums.QueueStatus.PAUSED_FOR_TEST) {
            log.info("Patient {} is at X-Ray (PAUSED_FOR_TEST), skipping room transfer", plan.getPatient().getId());
            return;
        }
        
        // Check if room is different
        if (newRoom.getId().equals(activeQueue.getClinicRoom().getId())) {
            log.info("Patient already in room {}, no transfer needed", newRoom.getName());
            return;
        }
        
        Long oldRoomId = activeQueue.getClinicRoom().getId();
        
        // Save original room if not set
        if (activeQueue.getOriginalRoomId() == null) {
            activeQueue.setOriginalRoomId(oldRoomId);
        }
        
        // Transfer to new room - preserve current status if RETURNED_PRIORITY
        activeQueue.setClinicRoom(newRoom);
        if (activeQueue.getStatus() != com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY) {
            activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.WAITING);
        }
        activeQueue.setPriorityLevel(activeQueue.getPriorityLevel() + 5); // Priority boost
        checkInQueueRepository.save(activeQueue);
        
        log.info("Patient {} transferred from room {} to room {}", 
            plan.getPatient().getId(), oldRoomId, newRoom.getId());
        
        // Send notification to patient
        String roomLocation = newRoom.getDescription() != null && !newRoom.getDescription().isBlank() 
                ? newRoom.getDescription() 
                : "Vui lòng hỏi nhân viên";
        
        String message = String.format(
            "Vui lòng di chuyển đến %s (%s) để tiếp tục điều trị.\n\n" +
            "🔢 Số thứ tự: %d\n\n" +
            "Bạn được ưu tiên trong hàng đợi.",
            newRoom.getName(),
            roomLocation,
            activeQueue.getQueueNumber()
        );

        com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                .patient(plan.getPatient())
                .title("🏥 Chuyển phòng khám")
                .message(message)
                .type("ROOM_TRANSFER")
                .build();
        notificationRepository.save(notif);
        
        if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
            fcmService.sendNotification(plan.getPatient().getFcmToken(), notif.getTitle(), notif.getMessage());
        }

        // Broadcast queue updates
        try {
            queueEventService.broadcastQueueUpdated(oldRoomId);
            queueEventService.broadcastQueueUpdated(newRoom.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast queue update", e);
        }
    }

    @Transactional
    public void cancelStep(Long stepId) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
        
        // CRITICAL FIX: Allow reopening COMPLETED steps for editing
        // When a step is COMPLETED and user wants to edit it, we need to reopen it
        // This is different from canceling an IN_PROGRESS step
        
        if (step.getStatus() == StepStatus.COMPLETED) {
            TreatmentPlan plan = step.getPlan();
            
            // Check for Invoice before allowing edit
            if (plan != null) {
                java.util.Optional<com.hcmute.clinic.entity.Invoice> invoice = invoiceRepository.findByTreatmentPlanId(plan.getId());
                if (invoice.isPresent()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Không thể chỉnh sửa bước điều trị vì Phác đồ này đã xuất Hóa đơn. Vui lòng Hủy Hóa đơn tại Quầy Kế Toán trước.");
                }
            }
            
            // Reopening a COMPLETED step for editing
            // This is allowed even if the plan is COMPLETED
            // because we want to allow doctors to fix mistakes or add more details
            
            if (plan != null && plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
                // Reopen the plan to IN_PROGRESS to allow updates
                plan.setStatus(TreatmentPlanStatus.IN_PROGRESS);
                planRepository.save(plan);
            }
            
            // Set step back to IN_PROGRESS (not PENDING) so it can be edited immediately
            step.setStatus(StepStatus.IN_PROGRESS);
            // Keep the existing data - don't clear doctorConclusion
            stepRepository.save(step);
            return;
        }
        
        // Original logic for canceling IN_PROGRESS steps
        if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        if (step.getStatus() != StepStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy bước đang thực hiện");
        }
        
        // Đặt lại về PENDING
        step.setStatus(StepStatus.PENDING);
        step.setDoctorConclusion(null); // Xóa kết luận nếu có
        stepRepository.save(step);
    }

    /**
     * DEPRECATED: Plans are now auto-activated on creation (no draft mode)
     * This method is kept for backward compatibility but does nothing
     * 
     * @deprecated Plans are automatically activated, no need to call this
     */
    @Deprecated
    @Transactional
    public void activatePlan(Long planId) {
        // NO-OP: Plans are now auto-activated on creation
        // Kept for backward compatibility with existing API calls
        log.info("activatePlan called for plan {} - NO-OP (plans are auto-activated)", planId);
    }


    /**
     * SE_14: Ghi nhận kết quả điều trị
     * Cập nhật kết luận, hình ảnh và đánh dấu hoàn thành bước này.
     */
    /**
     * SE_14: Ghi nhận kết quả điều trị (Treatment Result Recording).
     * Cập nhật kết luận bác sĩ, hình ảnh minh chứng và đánh dấu hoàn thành bước.
     */
    @Transactional
    public void updateStepResult(Long stepId, String conclusion, List<String> imageUrls, Long doctorRoomId) {
        log.info("[SE_14] Recording treatment result for step #{}", stepId);
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bước điều trị"));

        TreatmentPlan plan = step.getPlan();
        
        if (plan != null && plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        // Kiểm tra quyền: Bác sĩ phải ở đúng phòng
        if (doctorRoomId != null && step.getClinicRoom() != null) {
            if (!doctorRoomId.equals(step.getClinicRoom().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền hoàn thành bước này. Bước này thuộc về phòng khác.");
            }
        }

        boolean isReCompleting = (step.getStatus() == StepStatus.COMPLETED);
        
        // VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa (chỉ khi không phải re-complete)
        if (!isReCompleting) {
            boolean hasPreviousIncomplete = plan.getSteps().stream()
                    .filter(s -> s.getSequenceOrder() != null && step.getSequenceOrder() != null)
                    .filter(s -> s.getSequenceOrder() < step.getSequenceOrder())
                    .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
            
            if (hasPreviousIncomplete) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
            }
        }

        // FIX: Đảm bảo MedicalRecord tồn tại trước khi complete
        if (plan != null && plan.getMedicalRecord() == null && plan.getAppointment() != null) {
            MedicalRecord medicalRecord = medicalRecordRepository.findByAppointmentId(plan.getAppointment().getId())
                    .orElseGet(() -> {
                        MedicalRecord newRecord = MedicalRecord.builder()
                                .appointment(plan.getAppointment())
                                .patient(plan.getPatient())
                                .doctor(plan.getAppointment().getDoctor())
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                        return medicalRecordRepository.save(newRecord);
                    });
            plan.setMedicalRecord(medicalRecord);
            planRepository.save(plan);
        }

        step.setDoctorConclusion(conclusion);
        step.setStatus(StepStatus.COMPLETED);
        if (step.getCompletedAt() == null) {
            step.setCompletedAt(java.time.LocalDateTime.now());
        }

        // Handle images
        if (imageUrls != null) {
            if (step.getImages() == null) {
                step.setImages(new java.util.LinkedHashSet<>());
            } else {
                step.getImages().clear();
            }
            for (String url : imageUrls) {
                step.getImages().add(StepImage.builder().step(step).imageUrl(url).build());
            }
        }

        stepRepository.save(step);

        // Update MedicalRecord
        if (step.getPlan() != null && step.getPlan().getMedicalRecord() != null) {
            updateMedicalRecordFromSteps(step.getPlan());
        }
    }

    @Transactional
    public String completeCurrentAndStartNext(Long planId, Long currentStepId) {
        log.info("[SE_15] Moving patient to next step in plan #{} after step #{}", planId, currentStepId);
        TreatmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phác đồ"));

        // 1. Tìm bước tiếp theo (PENDING)
        TreatmentPlanStep nextStep = plan.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING)
                .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
                .orElse(null);

        if (nextStep != null) {
            nextStep.setStatus(StepStatus.IN_PROGRESS);
            stepRepository.save(nextStep);
        }

        // 2. Tra cứu hàng đợi hiện tại
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<CheckInQueue> queues = checkInQueueRepository.findTodayForPatient(
            plan.getPatient().getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay()
        );
        
        CheckInQueue activeQueue = queues.stream()
            .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.IN_PROGRESS 
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.PAUSED_FOR_TEST
                      || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY)
            .findFirst()
            .orElse(null);

        if (activeQueue == null) return null;

        ClinicRoom nextRoom = nextStep != null ? nextStep.getClinicRoom() : null;
        ClinicRoom currentRoom = activeQueue.getClinicRoom();
        
        // C1: Có bước tiếp theo và phải sang phòng khác
        if (nextRoom != null && !nextRoom.getId().equals(currentRoom.getId())) {
            Long oldRoomId = currentRoom.getId();
            if (activeQueue.getOriginalRoomId() == null) {
                activeQueue.setOriginalRoomId(oldRoomId);
            }
            activeQueue.setClinicRoom(nextRoom);
            
            // Check if returning to original room
            if (nextRoom.getId().equals(activeQueue.getOriginalRoomId())) {
                activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY);
                activeQueue.setPriorityLevel((activeQueue.getPriorityLevel() != null ? activeQueue.getPriorityLevel() : 0) + 10);
            } else if (activeQueue.getStatus() != com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY) {
                activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.WAITING);
                activeQueue.setPriorityLevel((activeQueue.getPriorityLevel() != null ? activeQueue.getPriorityLevel() : 0) + 5);
            }
            
            checkInQueueRepository.save(activeQueue);
            sendQueueNotification(plan, activeQueue, nextRoom, nextStep, nextRoom.getId().equals(activeQueue.getOriginalRoomId()));
            broadcastQueueUpdates(oldRoomId, nextRoom.getId());
            return nextRoom.getName();

        // C2: Không có bước tiếp theo ở phòng khác, trả về phòng khám gốc
        } else if ((nextRoom == null || nextRoom.getId().equals(currentRoom.getId())) 
                    && activeQueue.getOriginalRoomId() != null 
                    && !activeQueue.getOriginalRoomId().equals(currentRoom.getId())) {
            
            Long oldRoomId = currentRoom.getId();
            ClinicRoom origRoom = clinicRoomRepository.findById(activeQueue.getOriginalRoomId()).orElse(null);
            if (origRoom != null) {
                activeQueue.setClinicRoom(origRoom);
                activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY);
                activeQueue.setPriorityLevel((activeQueue.getPriorityLevel() != null ? activeQueue.getPriorityLevel() : 0) + 10);
                checkInQueueRepository.save(activeQueue);
                
                sendQueueNotification(plan, activeQueue, origRoom, nextStep, true);
                broadcastQueueUpdates(oldRoomId, origRoom.getId());
                return origRoom.getName();
            }
        }
        
        return null;
    }

    private void broadcastQueueUpdates(Long oldRoomId, Long newRoomId) {
        try {
            queueEventService.broadcastQueueUpdated(oldRoomId);
            queueEventService.broadcastQueueUpdated(newRoomId);
        } catch (Exception e) {}
    }

    private void sendQueueNotification(TreatmentPlan plan, CheckInQueue queue, ClinicRoom room, TreatmentPlanStep nextStep, boolean isReturn) {
        String title = isReturn ? "🏥 Trở lại phòng khám ban đầu" : "🏥 Chuyển phòng khám";
        String roomLocation = room.getDescription() != null && !room.getDescription().isBlank() ? room.getDescription() : "Vui lòng hỏi nhân viên";
        String serviceName = nextStep != null && nextStep.getService() != null ? nextStep.getService().getName() : "Khám tổng quát / Đọc kết quả";
        
        String message;
        if (isReturn) {
            message = "Vui lòng quay lại: " + room.getName() + " để tiếp tục điều trị. Bạn được ưu tiên gọi vào phòng.";
        } else {
            int estimatedWaitTime = 0;
            java.time.LocalDate today = java.time.LocalDate.now();
            java.util.List<CheckInQueue> waitingList = checkInQueueRepository.findByRoomAndDateRange(
                    room.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay(),
                    java.util.List.of(com.hcmute.clinic.enums.QueueStatus.WAITING, com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY));
            
            for (CheckInQueue q : waitingList) {
                if (q.getId().equals(queue.getId())) break;
                estimatedWaitTime += (q.getAppointment() != null && q.getAppointment().getService() != null && q.getAppointment().getService().getDurationMinutes() != null) 
                        ? q.getAppointment().getService().getDurationMinutes() : 15;
            }
            
            message = String.format(
                "Vui lòng di chuyển đến %s (%s) để tiếp tục điều trị.\n\n" +
                "📋 Dịch vụ tiếp theo: %s\n" +
                "🔢 Số thứ tự: %d\n" +
                "⏱️ Thời gian chờ dự kiến: ~%d phút\n\n" +
                "Bạn được ưu tiên trong hàng đợi.",
                room.getName(), roomLocation, serviceName, queue.getQueueNumber(), estimatedWaitTime
            );
        }

        Notification notif = Notification.builder()
                .patient(plan.getPatient())
                .title(title)
                .message(message)
                .type("ROOM_TRANSFER")
                .build();
        notificationRepository.save(notif);
        
        if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
            fcmService.sendNotification(plan.getPatient().getFcmToken(), notif.getTitle(), notif.getMessage());
        }
    }

    @Transactional
    public String completeStepAndAdvance(Long stepId, String conclusion, java.util.List<String> imageUrls, 
                                        Long doctorRoomId, 
                                        com.hcmute.clinic.repository.CheckInQueueRepository queueRepo,
                                        QueueEventService queueEventService,
                                        com.hcmute.clinic.repository.NotificationRepository notifRepo) {
        TreatmentPlanStep currentStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found"));
        
        TreatmentPlan plan = currentStep.getPlan();
        
        // Use the new updateStepResult logic
        updateStepResult(stepId, conclusion, imageUrls, doctorRoomId);
        
        // Re-fetch step after update
        currentStep = stepRepository.findById(stepId).get();
        
        // If re-completing, don't advance
        if (currentStep.getCompletedAt() != null && currentStep.getCompletedAt().isBefore(java.time.LocalDateTime.now().minusSeconds(5))) {
             return null;
        }
        
        // Advance logic using the newly aligned method, but handle legacy dependencies
        // Actually, completeCurrentAndStartNext covers exactly what we need
        return completeCurrentAndStartNext(plan.getId(), stepId);
    }
    
    /**
     * DEPRECATED: Use ServiceRoomAssignmentService.determineRoomForService() instead
     * This method is kept for backward compatibility but delegates to the centralized service
     * 
     * @deprecated Use {@link ServiceRoomAssignmentService#determineRoomForService(Service)} instead
     */
    @Deprecated
    private ClinicRoom findRoomForService(com.hcmute.clinic.entity.Service service) {
        return roomAssignmentService.determineRoomForService(service);
    }

    @Transactional
    public TreatmentPlanTemplateResponseDTO saveTemplate(Long id, TreatmentPlanTemplateRequest req) {
        log.info("Saving TreatmentPlanTemplate: {} (Action: {})", req.getName(), (id == null ? "CREATE" : "UPDATE"));
        TreatmentPlanTemplate template;
        if (id != null) {
            template = templateRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template không tồn tại"));
            template.getSteps().clear(); // Clear existing steps
        } else {
            template = new TreatmentPlanTemplate();
            template.setSteps(new java.util.LinkedHashSet<>());
        }

        template.setName(req.getName());
        template.setDescription(req.getDescription());
        template.setActive(req.isActive());

        if (req.getSteps() != null) {
            for (TreatmentPlanTemplateRequest.StepRequest stepReq : req.getSteps()) {
                com.hcmute.clinic.entity.Service sv = serviceRepository.findById(stepReq.getServiceId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dịch vụ không tồn tại: " + stepReq.getServiceId()));
                ClinicRoom room = null;
                if (stepReq.getClinicRoomId() != null) {
                    room = clinicRoomRepository.findById(stepReq.getClinicRoomId()).orElse(null);
                }
                
                TreatmentPlanTemplateStep tstep = TreatmentPlanTemplateStep.builder()
                        .template(template)
                        .service(sv)
                        .clinicRoom(room)
                        .sequenceOrder(stepReq.getSequenceOrder())
                        .medicationDetails(stepReq.getMedicationDetails())
                        .build();
                template.getSteps().add(tstep);
            }
        }
        // Use saveAndFlush to catch DB constraints / issues immediately
        TreatmentPlanTemplate saved = templateRepository.saveAndFlush(template);
        log.info("Successfully saved TreatmentPlanTemplate with ID: {}", saved.getId());
        return convertToResponseDTO(saved);
    }

    private TreatmentPlanTemplateResponseDTO convertToResponseDTO(TreatmentPlanTemplate entity) {
        List<TreatmentPlanTemplateResponseDTO.StepResponse> steps = entity.getSteps() != null ? entity.getSteps().stream()
                .sorted(Comparator.comparingInt(TreatmentPlanTemplateStep::getSequenceOrder))
                .map(s -> TreatmentPlanTemplateResponseDTO.StepResponse.builder()
                        .id(s.getId())
                        .serviceId(s.getService() != null ? s.getService().getId() : null) // ADDED: Return serviceId for editing
                        .serviceName(s.getService() != null ? s.getService().getName() : "")
                        .description(s.getService() != null ? s.getService().getDescription() : "")
                        .stepOrder(s.getSequenceOrder())
                        .estimatedPrice(s.getService() != null && s.getService().getPrice() != null ? s.getService().getPrice().doubleValue() : 0.0)
                        .estimatedDurationMinutes(s.getService() != null ? s.getService().getDurationMinutes() : 0)
                        .medicationDetails(s.getMedicationDetails())
                        .build())
                .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();

        return TreatmentPlanTemplateResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .steps(steps)
                .build();
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template không tồn tại");
        }
        templateRepository.deleteById(id);
    }
    
    /**
     * NEW: Aggregate all doctor notes from completed treatment steps into MedicalRecord
     * This ensures patient can see complete treatment history with step-by-step notes
     */
    private void updateMedicalRecordFromSteps(TreatmentPlan plan) {
        MedicalRecord record = plan.getMedicalRecord();
        if (record == null) {
            return;
        }
        
        // Aggregate all doctorConclusion from completed steps
        StringBuilder allNotes = new StringBuilder();
        List<TreatmentPlanStep> completedSteps = plan.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.COMPLETED && s.getDoctorConclusion() != null && !s.getDoctorConclusion().trim().isEmpty())
                .sorted(Comparator.comparing(s -> s.getCompletedAt() != null ? s.getCompletedAt() : java.time.LocalDateTime.MIN))
                .toList();
        
        for (TreatmentPlanStep step : completedSteps) {
            String serviceName = step.getService() != null ? step.getService().getName() : "Dịch vụ";
            String toothInfo = step.getToothNumber() != null && !step.getToothNumber().isEmpty() 
                    ? " (Răng " + step.getToothNumber() + ")" 
                    : "";
            
            allNotes.append("• ")
                    .append(serviceName)
                    .append(toothInfo)
                    .append(": ")
                    .append(step.getDoctorConclusion())
                    .append("\n");
        }
        
        // Update diagnosis with aggregated notes
        if (allNotes.length() > 0) {
            String aggregatedNotes = allNotes.toString().trim();
            
            // If diagnosis is empty or just placeholder text, replace it
            if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty() 
                    || record.getDiagnosis().equals("Khám tổng quát")) {
                record.setDiagnosis(aggregatedNotes);
            } else {
                // Append to existing diagnosis if it doesn't already contain the notes
                if (!record.getDiagnosis().contains(aggregatedNotes)) {
                    record.setDiagnosis(record.getDiagnosis() + "\n\n" + aggregatedNotes);
                }
            }
            
            medicalRecordRepository.save(record);
            log.info("Updated MedicalRecord {} with aggregated notes from {} completed steps", 
                    record.getId(), completedSteps.size());
        }
    }
}
