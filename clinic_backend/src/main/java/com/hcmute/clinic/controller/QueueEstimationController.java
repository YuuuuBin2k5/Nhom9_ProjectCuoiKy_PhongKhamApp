package com.hcmute.clinic.controller;

import com.hcmute.clinic.dto.queue.QueueEstimateDTO;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import com.hcmute.clinic.service.QueueEstimationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller cung cấp thông tin dự báo thời gian chờ đợi trong hàng đợi.
 * Giúp bệnh nhân chủ động sắp xếp thời gian khi đến phòng khám.
 */
@RestController
@RequestMapping("/api/queue/estimate")
@RequiredArgsConstructor
@Slf4j
public class QueueEstimationController {

    private final QueueEstimationService estimationService;
    private final CheckInQueueRepository queueRepo;

    /**
     * Get queue estimate for a specific queue item
     * Used by patient mobile app to show wait time
     */
    @GetMapping("/{queueId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<QueueEstimateDTO> getQueueEstimate(@PathVariable Long queueId) {
        log.debug("Getting queue estimate for queue ID: {}", queueId);

        CheckInQueue queue = queueRepo.findById(queueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Queue not found"
                ));

        QueueEstimateDTO estimate = estimationService.calculateEstimate(queue);

        return ResponseEntity.ok(estimate);
    }

    /**
     * Get queue estimate by appointment ID
     * Convenience endpoint for patients who know their appointment ID
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<QueueEstimateDTO> getQueueEstimateByAppointment(
            @PathVariable Long appointmentId) {
        log.debug("Getting queue estimate for appointment ID: {}", appointmentId);

        CheckInQueue queue = queueRepo.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No queue found for this appointment"
                ));

        QueueEstimateDTO estimate = estimationService.calculateEstimate(queue);

        return ResponseEntity.ok(estimate);
    }
}
