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
import java.util.Optional;

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
    private final FcmService fcmService;
    private final com.hcmute.clinic.repository.AppointmentRepository appointmentRepository;

    public List<TreatmentPlanTemplate> listActiveTemplates() {
        return templateRepository.findByActiveTrueOrderByNameAsc();
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
                .isDraft(true)
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

        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
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
                ClinicRoom room = null;
                if (item.getClinicRoomId() != null) {
                    room = clinicRoomRepository.findById(item.getClinicRoomId()).orElse(null);
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
    public void startStep(Long stepId) {
        TreatmentPlanStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước điều trị không tồn tại"));
        
        if (step.getPlan() != null && step.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        if (step.getStatus() == StepStatus.PENDING) {
            step.setStatus(StepStatus.IN_PROGRESS);
            stepRepository.save(step);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bước này không ở trạng thái chờ");
        }
    }

    @Transactional
    public void activatePlan(Long planId) {
        TreatmentPlan plan = getById(planId);
        
        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        if (!plan.isDraft()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phác đồ đã được kích hoạt");
        }
        if (plan.getSteps() == null || plan.getSteps().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phác đồ đang trống, không thể kích hoạt");
        }
        
        plan.setDraft(false);
        planRepository.save(plan);

        // Kích hoạt bước đầu tiên (PENDING -> IN_PROGRESS)
        TreatmentPlanStep firstStep = plan.getSteps().stream()
                .min((a, b) -> Integer.compare(
                        a.getSequenceOrder() != null ? a.getSequenceOrder() : 0,
                        b.getSequenceOrder() != null ? b.getSequenceOrder() : 0))
                .orElse(null);

        if (firstStep != null && firstStep.getStatus() == StepStatus.PENDING) {
            firstStep.setStatus(StepStatus.IN_PROGRESS);
            stepRepository.save(firstStep);
        }
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

        if (currentStep.getPlan() != null && currentStep.getPlan().getStatus() == TreatmentPlanStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ đã hoàn tất và bị khóa");
        }

        // Kiểm tra quyền: Bác sĩ phải ở đúng phòng
        if (doctorRoomId != null && currentStep.getClinicRoom() != null) {
            if (!doctorRoomId.equals(currentStep.getClinicRoom().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền hoàn thành bước này. Bước này thuộc về phòng khác.");
            }
        }

        if (currentStep.getStatus() == StepStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bước này đã hoàn thành");
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

        TreatmentPlan plan = currentStep.getPlan();
        
        // Tìm bước tiếp theo (chỉ PENDING, không IN_PROGRESS)
        TreatmentPlanStep nextStep = plan.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING)
                .min(Comparator.comparingInt(s -> s.getSequenceOrder() != null ? s.getSequenceOrder() : 0))
                .orElse(null);

        if (nextStep == null) {
            // Hoàn tất toàn bộ phác đồ - KHÔNG TỰ ĐỘNG SINH BƯỚC
            plan.setStatus(TreatmentPlanStatus.COMPLETED);
            planRepository.save(plan);

            com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                    .patient(plan.getPatient())
                    .title("Phác đồ hoàn tất")
                    .message("Phác đồ điều trị của bạn đã hoàn tất.")
                    .type("TREATMENT_COMPLETE")
                    .build();
            notifRepo.save(notif);
            if (plan.getPatient().getFcmToken() != null && !plan.getPatient().getFcmToken().isBlank()) {
                fcmService.sendNotification(plan.getPatient().getFcmToken(), notif.getTitle(), notif.getMessage());
            }
            return null; // Không còn bước nào
        }

        // Kích hoạt bước tiếp theo
        nextStep.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(nextStep);

        // Chuyển phòng nếu bước tiếp theo thuộc phòng khác
        ClinicRoom nextRoom = nextStep.getClinicRoom();
        if (nextRoom != null) {
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

                com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                        .patient(plan.getPatient())
                        .title("Chuyển phòng khám")
                        .message("Vui lòng di chuyển đến " + nextRoom.getName() + " để tiếp tục điều trị. Số TT: " + activeQueue.getQueueNumber())
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
        }
        
        return null;
    }
}
