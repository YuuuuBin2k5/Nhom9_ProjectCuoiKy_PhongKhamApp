package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Value;

/**
 * DTO cung cấp trạng thái hàng đợi thời gian thực cho bệnh nhân sau khi Check-in.
 * Giúp bệnh nhân theo dõi số thứ tự và thời gian chờ đợi dự kiến.
 */
@Value
@Builder
public class CheckInMyStatusResponse {
    boolean checkedIn;
    Integer queueNumber;
    Integer queuePosition;
    String roomName;
    String roomLocation;
    /** Mã enum: WAITING, IN_PROGRESS, … hoặc null nếu chưa check-in */
    String status;
    /** Nhãn tiếng Việt cho bệnh nhân */
    String statusLabel;
    Integer estimatedWaitTime;
    /** Gợi ý ngắn (UX) */
    String hint;
    
    // Queue estimation fields (new)
    String estimateDisplayType;
    Integer estimatedMinutes;
    Integer minMinutes;
    Integer maxMinutes;
    String estimateMessage;
    String estimateConfidence;
    Boolean showApproximateLabel;
    String estimateTitle;
    String estimateSubtitle;
    Integer countdownStartSeconds; // For countdown timer (position 1 only)
    
    // Treatment context fields (Phase 3 enhancement)
    String doctorName;
    String serviceName;
    Long treatmentPlanId;
    String currentStepName;
    Integer currentStepNumber;
    Integer totalSteps;
}
