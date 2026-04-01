package com.hcmute.clinic.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long appointmentId;
    private Long doctorId;
    private Long serviceId;
    private Integer rating; // 1-5
    private String comment;
}
