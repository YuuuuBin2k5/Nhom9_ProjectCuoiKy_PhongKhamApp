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
            plan.setSteps(new ArrayList<>());
        }

        if (template != null) {
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

        List<TreatmentPlanStep> existingSteps = plan.getSteps();
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
        
        // Kiểm tra quyền: Bác sĩ phải ở đúng phòng (nếu step có chỉ định phòng)
        if (doctorRoomId != null && step.getClinicRoom() != null) {
            if (!doctorRoomId.equals(step.getClinicRoom().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền bắt đầu bước này. Bước này thuộc về phòng khác.");
            }
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
        
        step.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(step);
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
     * FIX 4: Method đã được đơn giản hóa - Xóa logic tự động sinh bước
     * Hoàn thành bước điều trị hiện tại và chuyển sang bước tiếp theo
     */
    @Transactional
    public String completeStepAndAdvance(Long stepId, String doctorConclusion, List<String> imageUrls, Long doctorRoomId, 
                                         com.hcmute.clinic.repository.CheckInQueueRepository queueRepo, 
                                         com.hcmute.clinic.service.QueueEventService queueEventService, 
                                         com.hcmute.clinic.repository.NotificationRepository notifRepo) {
        TreatmentPlanStep currentStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước không tồn tại"));

        TreatmentPlan plan = currentStep.getPlan();
        
        if (plan != null && plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        // Kiểm tra quyền: Bác sĩ phải ở đúng phòng
        if (doctorRoomId != null && currentStep.getClinicRoom() != null) {
            if (!doctorRoomId.equals(currentStep.getClinicRoom().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền hoàn thành bước này. Bước này thuộc về phòng khác.");
            }
        }

        // PROFESSIONAL FIX: Cho phép re-complete một step đã COMPLETED (để edit)
        // NHƯNG: Không auto-advance sang step tiếp theo nếu đang re-complete
        boolean isReCompleting = (currentStep.getStatus() == StepStatus.COMPLETED);
        
        // VALIDATION: Kiểm tra các bước trước đó đã hoàn thành chưa (chỉ khi không phải re-complete)
        if (!isReCompleting) {
            boolean hasPreviousIncomplete = plan.getSteps().stream()
                    .filter(s -> s.getSequenceOrder() != null && currentStep.getSequenceOrder() != null)
                    .filter(s -> s.getSequenceOrder() < currentStep.getSequenceOrder())
                    .anyMatch(s -> s.getStatus() != StepStatus.COMPLETED && s.getStatus() != StepStatus.SKIPPED);
            
            if (hasPreviousIncomplete) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Không thể hoàn thành bước này. Vui lòng hoàn thành các bước trước đó theo thứ tự.");
            }
        }

        // FIX: Đảm bảo MedicalRecord tồn tại trước khi complete
        if (plan != null && plan.getMedicalRecord() == null && plan.getAppointment() != null) {
            // Tự động tạo MedicalRecord nếu chưa có
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

        // Hoàn thành bước hiện tại
        currentStep.setStatus(StepStatus.COMPLETED);
        currentStep.setCompletedAt(java.time.LocalDateTime.now());
        if (doctorConclusion != null) {
            currentStep.setDoctorConclusion(doctorConclusion);
        }
        
        // Lưu ảnh
        if (imageUrls != null && !imageUrls.isEmpty()) {
            if (currentStep.getImages() == null) currentStep.setImages(new ArrayList<>());
            for (String url : imageUrls) {
                StepImage img = StepImage.builder()
                        .step(currentStep)
                        .imageUrl(url)
                        .build();
                currentStep.getImages().add(img);
            }
        }
        
        stepRepository.save(currentStep);
        
        // PROFESSIONAL FIX: Nếu đang re-complete một step đã COMPLETED, không auto-advance
        if (isReCompleting) {
            System.out.println("TreatmentPlanService: Re-completing step " + stepId + " - không auto-advance");
            return null; // Không chuyển phòng, không advance
        }
        
        // Tìm bước tiếp theo (chỉ PENDING, không IN_PROGRESS)
        TreatmentPlanStep nextStep = plan.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING)
                .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
                .orElse(null);

        if (nextStep == null) {
            // Không còn PENDING steps
            // CHECK: Có step nào còn IN_PROGRESS không?
            boolean hasInProgress = plan.getSteps().stream()
                    .anyMatch(s -> s.getStatus() == StepStatus.IN_PROGRESS);
            
            // REMOVED AUTO-COMPLETE LOGIC:
            // Plan should only be completed when user explicitly clicks "Hoàn thành" button
            // NOT automatically when all steps are done
            // 
            // Old logic (removed):
            // if (!hasInProgress) {
            //     plan.setStatus(TreatmentPlanStatus.COMPLETED);
            //     planRepository.save(plan);
            //     ... cleanup queues and send notification ...
            // }
            
            // Now: Just return null, don't auto-complete the plan
            return null;
        }

        // Kích hoạt bước tiếp theo
        nextStep.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(nextStep);

        // Chuyển phòng nếu bước tiếp theo thuộc phòng khác
        // NHƯNG: Không chuyển phòng nếu bước TIẾP THEO là step đầu tiên (sequenceOrder = 0)
        // Vì đó là lần đầu tiên bắt đầu điều trị, bệnh nhân vẫn ở phòng ban đầu
        boolean isNextStepFirst = nextStep.getSequenceOrder() != null && nextStep.getSequenceOrder() == 0;
        
        ClinicRoom nextRoom = nextStep.getClinicRoom();
        if (nextRoom != null && !isNextStepFirst) {
            // Lấy hàng đợi hiện tại của bệnh nhân
            java.util.List<com.hcmute.clinic.entity.CheckInQueue> queues = queueRepo.findTodayForPatient(
                plan.getPatient().getId(), 
                java.time.LocalDate.now().atStartOfDay(), 
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
            );
            com.hcmute.clinic.entity.CheckInQueue activeQueue = queues.stream()
                .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.IN_PROGRESS 
                          || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING)
                .findFirst()
                .orElse(null);
            
            if (activeQueue != null && !nextRoom.getId().equals(activeQueue.getClinicRoom().getId())) {
                Long oldRoomId = activeQueue.getClinicRoom().getId();
                
                // Lưu originalRoomId nếu chưa có
                if (activeQueue.getOriginalRoomId() == null) {
                    activeQueue.setOriginalRoomId(oldRoomId);
                }
                
                activeQueue.setClinicRoom(nextRoom);
                activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.WAITING);
                activeQueue.setPriorityLevel(activeQueue.getPriorityLevel() + 5); 
                queueRepo.save(activeQueue);

                // Calculate estimated wait time
                int estimatedWaitTime = 0;
                java.time.LocalDate today = java.time.LocalDate.now();
                java.util.List<com.hcmute.clinic.entity.CheckInQueue> waitingList = queueRepo.findByRoomAndDateRange(
                        nextRoom.getId(),
                        today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay(),
                        java.util.List.of(com.hcmute.clinic.enums.QueueStatus.WAITING, com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY));
                
                for (com.hcmute.clinic.entity.CheckInQueue q : waitingList) {
                    if (q.getId().equals(activeQueue.getId())) {
                        break;
                    }
                    if (q.getAppointment() != null && q.getAppointment().getService() != null && q.getAppointment().getService().getDurationMinutes() != null) {
                        estimatedWaitTime += q.getAppointment().getService().getDurationMinutes();
                    } else {
                        estimatedWaitTime += 15;
                    }
                }

                // Build detailed notification message
                String roomLocation = nextRoom.getDescription() != null && !nextRoom.getDescription().isBlank() 
                        ? nextRoom.getDescription() 
                        : "Vui lòng hỏi nhân viên";
                
                String message = String.format(
                    "Vui lòng di chuyển đến %s (%s) để tiếp tục điều trị.\n\n" +
                    "📋 Dịch vụ tiếp theo: %s\n" +
                    "🔢 Số thứ tự: %d\n" +
                    "⏱️ Thời gian chờ dự kiến: ~%d phút\n\n" +
                    "Bạn được ưu tiên trong hàng đợi.",
                    nextRoom.getName(),
                    roomLocation,
                    nextStep.getService().getName(),
                    activeQueue.getQueueNumber(),
                    estimatedWaitTime
                );

                com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                        .patient(plan.getPatient())
                        .title("🏥 Chuyển phòng khám")
                        .message(message)
                        .type("ROOM_TRANSFER")
                        .build();
                notifRepo.save(notif);
                
                if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
                    fcmService.sendNotification(plan.getPatient().getFcmToken(), notif.getTitle(), notif.getMessage());
                }

                try {
                    queueEventService.broadcastQueueUpdated(oldRoomId);
                    queueEventService.broadcastQueueUpdated(nextRoom.getId());
                } catch (Exception e) {}

                return nextRoom.getName();
            }
        } else if (!isNextStepFirst) {
            // Trả bệnh nhân về phòng khám gốc sau khi làm xong dịch vụ nhánh (dangling queue fix)
            java.util.List<com.hcmute.clinic.entity.CheckInQueue> queuesReturn = queueRepo.findTodayForPatient(
                plan.getPatient().getId(), 
                java.time.LocalDate.now().atStartOfDay(), 
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
            );
            com.hcmute.clinic.entity.CheckInQueue activeQueue = queuesReturn.stream()
                .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.IN_PROGRESS 
                          || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING)
                .findFirst()
                .orElse(null);
                
            if (activeQueue != null && activeQueue.getOriginalRoomId() != null && 
                !activeQueue.getOriginalRoomId().equals(activeQueue.getClinicRoom().getId())) {
                
                Long oldRoomId = activeQueue.getClinicRoom().getId();
                ClinicRoom origRoom = clinicRoomRepository.findById(activeQueue.getOriginalRoomId()).orElse(null);
                
                if (origRoom != null) {
                    activeQueue.setClinicRoom(origRoom);
                    activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.RETURNED_PRIORITY);
                    activeQueue.setPriorityLevel(activeQueue.getPriorityLevel() + 10);
                    queueRepo.save(activeQueue);
                    
                    com.hcmute.clinic.entity.Notification notifRet = com.hcmute.clinic.entity.Notification.builder()
                            .patient(plan.getPatient())
                            .title("🏥 Trở lại phòng khám ban đầu")
                            .message("Vui lòng quay lại: " + origRoom.getName() + " để tiếp tục điều trị. Bạn được ưu tiên gọi vào phòng.")
                            .type("ROOM_TRANSFER")
                            .build();
                    notifRepo.save(notifRet);
                    
                    if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
                        fcmService.sendNotification(plan.getPatient().getFcmToken(), notifRet.getTitle(), notifRet.getMessage());
                    }

                    try {
                        queueEventService.broadcastQueueUpdated(oldRoomId);
                        queueEventService.broadcastQueueUpdated(origRoom.getId());
                    } catch (Exception e) {}
                    
                    return origRoom.getName();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Auto-assign clinic room based on service name/type
     * This allows manually added services to be assigned to the correct room
     */
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
            template.setSteps(new ArrayList<>());
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
}
