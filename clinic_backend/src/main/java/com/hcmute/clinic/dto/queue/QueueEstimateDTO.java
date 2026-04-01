package com.hcmute.clinic.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEstimateDTO {
    private String displayType; // IN_PROGRESS, SOFT_COUNTDOWN, RANGE
    private Integer position;
    private Integer estimatedMinutes;
    private Integer minMinutes;
    private Integer maxMinutes;
    private String message;
    private String confidence; // HIGH, MEDIUM, LOW
    private Boolean showApproximateLabel;
    private LocalDateTime lastUpdated;
    
    // For mobile display
    private String title;
    private String subtitle;
    private String statusColor;
    
    // For countdown timer (position 1 only)
    private Integer countdownStartSeconds;
}
