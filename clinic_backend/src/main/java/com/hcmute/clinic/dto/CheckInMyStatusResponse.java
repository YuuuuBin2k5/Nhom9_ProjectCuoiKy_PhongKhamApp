package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Value;

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
}
