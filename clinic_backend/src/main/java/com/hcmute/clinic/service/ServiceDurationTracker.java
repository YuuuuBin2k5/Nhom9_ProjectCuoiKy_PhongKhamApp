package com.hcmute.clinic.service;

import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.ServiceDurationHistory;
import com.hcmute.clinic.repository.ServiceDurationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceDurationTracker {

    private final ServiceDurationHistoryRepository historyRepo;

    /**
     * Record actual service duration when queue item is completed
     */
    @Transactional
    public void recordDuration(CheckInQueue queue) {
        if (queue.getStartedAt() == null || queue.getCompletedAt() == null) {
            log.warn("Cannot record duration for queue {}: missing timestamps", queue.getId());
            return;
        }

        if (queue.getAppointment() == null || queue.getAppointment().getService() == null) {
            log.warn("Cannot record duration for queue {}: missing service", queue.getId());
            return;
        }

        try {
            int actualDuration = (int) Duration.between(
                    queue.getStartedAt(),
                    queue.getCompletedAt()
            ).toMinutes();

            Integer scheduledDuration = queue.getAppointment().getService().getDurationMinutes();
            if (scheduledDuration == null) {
                scheduledDuration = 15; // default
            }

            ServiceDurationHistory history = ServiceDurationHistory.builder()
                    .service(queue.getAppointment().getService())
                    .appointment(queue.getAppointment())
                    .scheduledDuration(scheduledDuration)
                    .actualDuration(actualDuration)
                    .startedAt(queue.getStartedAt())
                    .completedAt(queue.getCompletedAt())
                    .doctor(queue.getAppointment().getDoctor())
                    .hadComplications(false) // Can be enhanced later
                    .build();

            historyRepo.save(history);

            log.info("Recorded service duration: service={}, scheduled={}, actual={}, variance={}",
                    queue.getAppointment().getService().getId(),
                    scheduledDuration,
                    actualDuration,
                    actualDuration - scheduledDuration);

        } catch (Exception e) {
            log.error("Failed to record service duration for queue {}", queue.getId(), e);
        }
    }

    /**
     * Mark queue as started (when status changes to IN_PROGRESS)
     */
    @Transactional
    public void markStarted(CheckInQueue queue) {
        if (queue.getStartedAt() == null) {
            queue.setStartedAt(LocalDateTime.now());
            log.debug("Marked queue {} as started", queue.getId());
        }
    }

    /**
     * Mark queue as completed
     */
    @Transactional
    public void markCompleted(CheckInQueue queue) {
        if (queue.getCompletedAt() == null) {
            queue.setCompletedAt(LocalDateTime.now());
            log.debug("Marked queue {} as completed", queue.getId());
        }
        recordDuration(queue);
    }
}
