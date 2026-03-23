package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CheckInMyStatusResponse {
    boolean checkedIn;
    Integer queueNumber;
    String roomName;
    String roomLocation;
    /** Mã enum: WAITING, IN_PROGRESS, … hoặc null nếu chưa check-in */
    String status;
    /** Nhãn tiếng Việt cho bệnh nhân */
    String statusLabel;
    /** Gợi ý ngắn (UX) */
    String hint;
}
