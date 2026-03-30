package com.hcmute.clinic.service;

import com.hcmute.clinic.dto.queue.ServiceVarianceStats;
import com.hcmute.clinic.entity.ServiceDurationHistory;
import com.hcmute.clinic.repository.ServiceDurationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueStatisticsService {

    private final ServiceDurationHistoryRepository historyRepo;

    @Cacheable(value = "serviceVariance", key = "#serviceId")
    public ServiceVarianceStats calculateVariance(Long serviceId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<ServiceDurationHistory> history = historyRepo
                .findByServiceIdAndCompletedAtAfter(serviceId, thirtyDaysAgo);

        if (history.isEmpty()) {
            log.debug("No historical data for service {}, using defaults", serviceId);
            return ServiceVarianceStats.defaultStats();
        }

        // Calculate statistics
        DoubleSummaryStatistics stats = history.stream()
                .mapToDouble(ServiceDurationHistory::getActualDuration)
                .summaryStatistics();

        double mean = stats.getAverage();
        double variance = history.stream()
                .mapToDouble(h -> Math.pow(h.getActualDuration() - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Calculate percentiles
        List<Integer> sortedDurations = history.stream()
                .map(ServiceDurationHistory::getActualDuration)
                .sorted()
                .collect(Collectors.toList());

        int p50 = getPercentile(sortedDurations, 50);
        int p75 = getPercentile(sortedDurations, 75);
        int p90 = getPercentile(sortedDurations, 90);

        String confidence = calculateConfidence(history.size());

        log.debug("Service {} stats: mean={}, stdDev={}, median={}, samples={}, confidence={}",
                serviceId, mean, stdDev, p50, history.size(), confidence);

        return ServiceVarianceStats.builder()
                .serviceId(serviceId)
                .mean(mean)
                .stdDev(stdDev)
                .median(p50)
                .p75(p75)
                .p90(p90)
                .sampleSize(history.size())
                .confidenceLevel(confidence)
                .build();
    }

    private int getPercentile(List<Integer> sorted, int percentile) {
        if (sorted.isEmpty()) return 15; // default
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private String calculateConfidence(int sampleSize) {
        if (sampleSize >= 100) return "HIGH";
        if (sampleSize >= 30) return "MEDIUM";
        return "LOW";
    }
}
