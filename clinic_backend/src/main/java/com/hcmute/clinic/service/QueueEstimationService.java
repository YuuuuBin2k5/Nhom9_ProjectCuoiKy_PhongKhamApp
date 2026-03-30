package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.queue.QueueEstimateDTO;
import com.hcmute.clinic.dto.queue.ServiceVarianceStats;
import com.hcmute.clinic.entity.CheckInQueue;
import com.hcmute.clinic.entity.Service;
import com.hcmute.clinic.enums.QueueStatus;
import com.hcmute.clinic.repository.CheckInQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class QueueEstimationService {

    private final QueueStatisticsService statsService;
    private final CheckInQueueRepository queueRepo;

    public QueueEstimateDTO calculateEstimate(CheckInQueue current) {
        int position = calculatePosition(current);

        // Position 0: Currently being served
        if (position == 0) {
            return createInProgressDisplay();
        }

        // Position 1: Next in line - SOFT countdown only
        if (position == 1) {
            return calculateSoftCountdown(current);
        }

        // Position 2+: Range estimation
        return calculateRangeEstimate(current, position);
    }

    private int calculatePosition(CheckInQueue current) {
        LocalDate today = LocalDate.now();
        List<CheckInQueue> queue = queueRepo.findByRoomAndDateRange(
                current.getClinicRoom().getId(),
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS, QueueStatus.RETURNED_PRIORITY)
        );

        // Sort by priority and queue number
        queue.sort((a, b) -> {
            // IN_PROGRESS always first
            if (a.getStatus() == QueueStatus.IN_PROGRESS) return -1;
            if (b.getStatus() == QueueStatus.IN_PROGRESS) return 1;

            // Then by priority level (higher first)
            int priorityCompare = Integer.compare(
                    b.getPriorityLevel() != null ? b.getPriorityLevel() : 0,
                    a.getPriorityLevel() != null ? a.getPriorityLevel() : 0
            );
            if (priorityCompare != 0) return priorityCompare;

            // Then by queue number
            return Integer.compare(a.getQueueNumber(), b.getQueueNumber());
        });

        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(current.getId())) {
                return i;
            }
        }

        return queue.size(); // Shouldn't happen
    }

    private QueueEstimateDTO createInProgressDisplay() {
        return QueueEstimateDTO.builder()
                .displayType("IN_PROGRESS")
                .position(0)
                .message("Đang khám")
                .title("Đang khám")
                .subtitle("Bác sĩ đang khám bệnh cho bạn")
                .statusColor("#4CAF50")
                .showApproximateLabel(false)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private QueueEstimateDTO calculateSoftCountdown(CheckInQueue current) {
        CheckInQueue inProgress = getInProgressPatient(current.getClinicRoom().getId());

        if (inProgress == null) {
            // No one in progress, you're next immediately
            int countdownSeconds = 2 * 60; // 2 minutes in seconds
            return QueueEstimateDTO.builder()
                    .displayType("SOFT_COUNTDOWN")
                    .position(1)
                    .estimatedMinutes(2)
                    .minMinutes(1)
                    .maxMinutes(5)
                    .message("Bạn sắp đến lượt")
                    .title("Bạn kế tiếp")
                    .subtitle("~2 phút")
                    .confidence("MEDIUM")
                    .statusColor("#FF9800")
                    .showApproximateLabel(true)
                    .countdownStartSeconds(countdownSeconds)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }

        Service service = inProgress.getAppointment().getService();
        ServiceVarianceStats stats = statsService.calculateVariance(service.getId());

        // Use P75 for conservative estimate, assume 50% remaining
        int remainingTime = Math.max(2, stats.getP75() / 2);
        int variance = (int) Math.ceil(stats.getStdDev() / 2);
        int countdownSeconds = remainingTime * 60; // Convert to seconds for countdown

        return QueueEstimateDTO.builder()
                .displayType("SOFT_COUNTDOWN")
                .position(1)
                .estimatedMinutes(remainingTime)
                .minMinutes(Math.max(1, remainingTime - variance))
                .maxMinutes(remainingTime + variance)
                .message("Bạn kế tiếp - vui lòng ở gần")
                .title("Bạn kế tiếp")
                .subtitle(String.format("~%d phút", remainingTime))
                .confidence(stats.getConfidenceLevel())
                .statusColor("#FF9800")
                .showApproximateLabel(true)
                .countdownStartSeconds(countdownSeconds)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private QueueEstimateDTO calculateRangeEstimate(CheckInQueue current, int position) {
        List<CheckInQueue> queueAhead = getQueueAhead(current);

        int totalMin = 0;
        int totalMax = 0;

        // Sum up all patients ahead
        for (CheckInQueue q : queueAhead) {
            if (q.getStatus() == QueueStatus.IN_PROGRESS) {
                // Current patient: use 50% of time
                Service service = q.getAppointment().getService();
                ServiceVarianceStats stats = statsService.calculateVariance(service.getId());
                totalMin += stats.getMedian() / 2;
                totalMax += stats.getP90() / 2;
            } else {
                // Waiting patients: use full time
                Service service = q.getAppointment().getService();
                ServiceVarianceStats stats = statsService.calculateVariance(service.getId());
                totalMin += stats.getMedian();
                totalMax += stats.getP90();
            }
        }

        String confidence = calculateOverallConfidence(queueAhead.size());

        return QueueEstimateDTO.builder()
                .displayType("RANGE")
                .position(position)
                .minMinutes(totalMin)
                .maxMinutes(totalMax)
                .estimatedMinutes((totalMin + totalMax) / 2)
                .message(getMessageForPosition(position))
                .title(String.format("Vị trí: #%d", position))
                .subtitle(String.format("%d-%d phút", totalMin, totalMax))
                .confidence(confidence)
                .statusColor("#2196F3")
                .showApproximateLabel(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private CheckInQueue getInProgressPatient(Long roomId) {
        LocalDate today = LocalDate.now();
        List<CheckInQueue> inProgress = queueRepo.findByRoomAndDateRange(
                roomId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                List.of(QueueStatus.IN_PROGRESS)
        );
        return inProgress.isEmpty() ? null : inProgress.get(0);
    }

    private List<CheckInQueue> getQueueAhead(CheckInQueue current) {
        LocalDate today = LocalDate.now();
        List<CheckInQueue> queue = queueRepo.findByRoomAndDateRange(
                current.getClinicRoom().getId(),
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                List.of(QueueStatus.WAITING, QueueStatus.IN_PROGRESS, QueueStatus.RETURNED_PRIORITY)
        );

        // Sort and filter to get only those ahead
        queue.sort((a, b) -> {
            if (a.getStatus() == QueueStatus.IN_PROGRESS) return -1;
            if (b.getStatus() == QueueStatus.IN_PROGRESS) return 1;
            int priorityCompare = Integer.compare(
                    b.getPriorityLevel() != null ? b.getPriorityLevel() : 0,
                    a.getPriorityLevel() != null ? a.getPriorityLevel() : 0
            );
            if (priorityCompare != 0) return priorityCompare;
            return Integer.compare(a.getQueueNumber(), b.getQueueNumber());
        });

        int currentIndex = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(current.getId())) {
                currentIndex = i;
                break;
            }
        }

        return currentIndex >= 0 ? queue.subList(0, currentIndex) : queue;
    }

    private String calculateOverallConfidence(int queueSize) {
        if (queueSize <= 2) return "HIGH";
        if (queueSize <= 5) return "MEDIUM";
        return "LOW";
    }

    private String getMessageForPosition(int position) {
        if (position <= 3) return "Thời gian ước tính";
        if (position <= 5) return "Vui lòng chờ trong khu vực";
        return "Thời gian chờ có thể thay đổi";
    }
}
