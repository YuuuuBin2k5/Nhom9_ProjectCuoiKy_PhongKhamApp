package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.Appointment;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.Notification;
import com.hcmute.clinic.enums.AppointmentStatus;
import com.hcmute.clinic.enums.QueueStatus;
import com.hcmute.clinic.repository.AppointmentRepository;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import com.hcmute.clinic.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final AppointmentRepository appointmentRepository;
    private final CheckInQueueRepository checkInQueueRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    private final CheckInQueueService checkInQueueService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendOneHourReminders() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Reminder Check Triggered. Server Time: {}", now);
        
        LocalDateTime windowStart = now;
        LocalDateTime windowEnd = now.plusHours(1);

        List<Appointment> appointments = appointmentRepository
                .findByStatusAndAppointmentDatetimeBetween(
                        AppointmentStatus.SCHEDULED, windowStart, windowEnd);

        for (Appointment app : appointments) {
            String trackingType = "APPT_REMINDER_" + app.getId();
            if (notificationRepository.existsByType(trackingType)) {
                continue;
            }

            String title = "Nhắc nhở lịch hẹn";
            String serviceName = app.getService() != null ? app.getService().getName() : "khám";
            String message = "Lịch hẹn " + serviceName + " của bạn sẽ bắt đầu trong khoảng 1 giờ nữa.";
            
            try {
                sendPushAndSaveNotif(app.getPatient(), title, message, trackingType);
                log.info("Sent persistent 1-hour reminder for appointment ID: {}", app.getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for appt {}: {}", app.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void sendFiveMinQueueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();

        List<CheckInQueue> waitingQueues = checkInQueueRepository.findByStatusInAndCheckInTimeBetween(
                List.of(QueueStatus.WAITING, QueueStatus.RETURNED_PRIORITY),
                startOfDay, endOfDay);

        for (CheckInQueue q : waitingQueues) {
            String trackingType = "QUEUE_ALERT_" + q.getId();
            if (notificationRepository.existsByType(trackingType)) continue;

            int waitTime = checkInQueueService.calculateEstimatedWaitTime(q);
            if (waitTime <= 6 && waitTime > 0) {
                String title = "Sắp đến lượt khám";
                String message = "Chỉ còn khoảng " + waitTime + " phút nữa là đến lượt khám của bạn. Vui lòng chuẩn bị sẵn sàng.";
                
                sendPushAndSaveNotif(q.getAppointment().getPatient(), title, message, trackingType);
                log.info("Sent persistent 5-minute queue reminder for queue ID: {}", q.getId());
            }
        }
    }

    private void sendPushAndSaveNotif(com.hcmute.clinic.entity.Patient patient, String title, String message, String type) {
        Notification notif = Notification.builder()
                .patient(patient)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        if (patient != null && patient.getFcmToken() != null && !patient.getFcmToken().isBlank()) {
            fcmService.sendNotification(patient.getFcmToken(), title, message);
        }
    }
}
