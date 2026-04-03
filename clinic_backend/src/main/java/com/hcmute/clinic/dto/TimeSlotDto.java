package com.hcmute.clinic.dto;

import lombok.*;

/**
 * DTO đại diện cho một khung giờ đặt lịch khả dụng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    private String time; // "08:00", "08:30", etc.
    private boolean available;
    private String reason; // Optional: why not available
}
