// FIX 4: Method completeStepAndAdvance() đã được đơn giản hóa
// Thay thế method cũ trong TreatmentPlanService.java

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

    // FIX 4: Kiểm tra quyền đơn giản hơn
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
    
    // Tìm bước tiếp theo
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
