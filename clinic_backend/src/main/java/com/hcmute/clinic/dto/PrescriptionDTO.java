package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private Long id;
    private Long medicalRecordId;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime createdAt;
    private String diagnosis;
    private String symptoms;
    private String advice;
    private List<DetailDTO> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailDTO {
        private Long id;
        private Long treatmentPlanStepId;
        private String medicineName;
        private String dosage;
        private String frequency;
        private String duration;
        private String unit;
    }
}
