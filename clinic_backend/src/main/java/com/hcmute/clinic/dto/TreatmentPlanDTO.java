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
public class TreatmentPlanDTO {
    private Long id;
    private Long patientId;
    private String status;
    private boolean isDraft;
    private List<StepDTO> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDTO {
        private Long id;
        private Long treatmentPlanId;
        private Long serviceId;
        private String serviceName;
        private String description;
        private Integer stepOrder;
        private String status;
        private String toothNumber;
        private Double estimatedPrice;
        private Double actualPrice;
        private String doctorConclusion;
        private String uiTemplateType;
        private String roomName;
        private String medicationDetails;
        private boolean editable;
    }
}
