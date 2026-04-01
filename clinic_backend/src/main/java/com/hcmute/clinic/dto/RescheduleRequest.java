package com.hcmute.clinic.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {
    private LocalDateTime newDatetime;
    private String reason;
}
