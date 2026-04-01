package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.Notification;
import com.hcmute.clinic.entity.Patient;
import com.hcmute.clinic.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Patient patient, String title, String message, String type) {
        Notification notification = Notification.builder()
                .patient(patient)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}
