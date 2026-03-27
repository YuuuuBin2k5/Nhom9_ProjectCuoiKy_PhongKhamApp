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
                .isDraft(true)
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
                // Create new
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

    @Transactional
    public String completeStepAndAdvance(Long stepId, String doctorConclusion, Long doctorRoomId, com.hcmute.clinic.repository.CheckInQueueRepository queueRepo, com.hcmute.clinic.service.QueueEventService queueEventService, com.hcmute.clinic.repository.NotificationRepository notifRepo) {
        TreatmentPlanStep currentStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bước không tồn tại"));

        if (doctorRoomId != null) {
            Long stepRoomId = currentStep.getClinicRoom() != null ? currentStep.getClinicRoom().getId() : null;
            if (!doctorRoomId.equals(stepRoomId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật bước điều trị này");
            }
        }

        if (currentStep.getStatus() == StepStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bước này đã hoàn thành");
        }

        currentStep.setStatus(StepStatus.COMPLETED);
        if (doctorConclusion != null) {
            currentStep.setDoctorConclusion(doctorConclusion);
        }
        stepRepository.save(currentStep);

        TreatmentPlan plan = currentStep.getPlan();
        
        // Find next step
        TreatmentPlanStep nextStep = plan.getSteps().stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING || s.getStatus() == StepStatus.IN_PROGRESS)
                .min((a, b) -> Integer.compare(
                        a.getSequenceOrder() != null ? a.getSequenceOrder() : 0,
                        b.getSequenceOrder() != null ? b.getSequenceOrder() : 0))
                .orElse(null);

        if (nextStep == null) {
            // All steps completed
            plan.setStatus(com.hcmute.clinic.enums.TreatmentPlanStatus.COMPLETED);
            planRepository.save(plan);

            // Notify patient
            com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                    .patient(plan.getPatient())
                    .title("Phác đồ hoàn tất")
                    .message("Phác đồ điều trị của bạn đã hoàn tất.")
                    .type("TREATMENT_COMPLETE")
                    .build();
            notifRepo.save(notif);
            return null; // No next room
        }

        // Activate next step
        nextStep.setStatus(StepStatus.IN_PROGRESS);
        stepRepository.save(nextStep);

        // Handle room transfer if the next step has a specific room
        ClinicRoom nextRoom = nextStep.getClinicRoom();
        if (nextRoom != null) {
            Long currentRoomId = currentStep.getClinicRoom() != null ? currentStep.getClinicRoom().getId() : null;
            
            // Tìm queue hiện tại của bệnh nhân
            // Assuming we only have one active appointment/queue per patient per day
            java.util.List<com.hcmute.clinic.entity.CheckInQueue> queues = queueRepo.findTodayForPatient(
                plan.getPatient().getId(), 
                java.time.LocalDate.now().atStartOfDay(), 
                java.time.LocalDate.now().plusDays(1).atStartOfDay()
            );

            // Tìm queue đang IN_PROGRESS hoặc WAITING
            com.hcmute.clinic.entity.CheckInQueue activeQueue = queues.stream()
                .filter(q -> q.getStatus() == com.hcmute.clinic.enums.QueueStatus.IN_PROGRESS || q.getStatus() == com.hcmute.clinic.enums.QueueStatus.WAITING)
                .findFirst()
                .orElse(null);

            if (activeQueue != null && !nextRoom.getId().equals(activeQueue.getClinicRoom().getId())) {
                Long oldRoomId = activeQueue.getClinicRoom().getId();
                
                // Cập nhật queue sang phòng mới
                activeQueue.setClinicRoom(nextRoom);
                activeQueue.setStatus(com.hcmute.clinic.enums.QueueStatus.WAITING);
                activeQueue.setPriorityLevel(activeQueue.getPriorityLevel() + 5); // Ưu tiên nhẹ vì đang điều trị dở dang
                queueRepo.save(activeQueue);

                // Gửi thông báo
                com.hcmute.clinic.entity.Notification notif = com.hcmute.clinic.entity.Notification.builder()
                        .patient(plan.getPatient())
                        .title("Chuyển phòng khám")
                        .message("Vui lòng di chuyển đến " + nextRoom.getName() + " để tiếp tục điều trị. Số TT: " + activeQueue.getQueueNumber())
                        .type("ROOM_TRANSFER")
                        .build();
                notifRepo.save(notif);

                // Broadcast update cho cả 2 phòng (cũ mất đi, mới thêm vào)
                try {
                    queueEventService.broadcastQueueUpdated(oldRoomId);
                    queueEventService.broadcastQueueUpdated(nextRoom.getId());
                } catch (Exception e) {
                    // ignore
                }

                return nextRoom.getName();
            }
        }
        
        return null;
    }
}
