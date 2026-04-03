package com.hcmute.clinic.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO yêu cầu tạo hoặc cập nhật mẫu phác đồ điều trị (UC_13).
 */
@Data
public class TreatmentPlanTemplateRequest {
    private String name;
    private String description;
    @com.fasterxml.jackson.annotation.JsonProperty("active")
    private boolean active = true;
    private List<StepRequest> steps;

    @Data
    public static class StepRequest {
        private Long serviceId;
        private Long clinicRoomId;
        private Integer sequenceOrder;
        private String medicationDetails;
    }
}
