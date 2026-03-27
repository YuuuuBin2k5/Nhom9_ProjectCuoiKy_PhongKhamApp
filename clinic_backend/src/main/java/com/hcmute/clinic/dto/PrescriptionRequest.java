package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    private Long appointmentId;
    private String diagnosis;
    private String symptoms;
    private String advice;
    private List<DetailRequest> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailRequest {
        private String medicineName;
        private String dosage;
        private String frequency;
        private String duration;
        private String unit;
    }
}
