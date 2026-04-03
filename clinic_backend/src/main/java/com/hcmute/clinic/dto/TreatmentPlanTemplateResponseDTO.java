package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO phản hồi chứa thông tin chi tiết về mẫu phác đồ điều trị.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentPlanTemplateResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String uiTemplateType;
    private Boolean isActive;
    private List<StepResponse> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepResponse {
        private Long id;
        private Long serviceId; // ADDED: For editing templates
        private String serviceName;
        private String description;
        private Integer stepOrder;
        private Double estimatedPrice;
        private Integer estimatedDurationMinutes;
        private String medicationDetails;
    }
}
