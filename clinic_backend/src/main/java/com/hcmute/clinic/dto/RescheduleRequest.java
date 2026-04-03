package com.hcmute.clinic.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO yêu cầu đổi lịch hẹn sang một khung giờ khác.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {
    private LocalDateTime newDatetime;
    private String reason;
}
