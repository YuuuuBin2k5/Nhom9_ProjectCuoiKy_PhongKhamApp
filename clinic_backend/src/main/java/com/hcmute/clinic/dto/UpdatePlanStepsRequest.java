package com.hcmute.clinic.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePlanStepsRequest {
    private List<StepItem> steps;

    @Data
    public static class StepItem {
        private Long id;
        private Long serviceId;
        private Long clinicRoomId;
        private Integer sequenceOrder;
        private String toothNumber; // FDI, e.g. "38" for SURGERY
        private String doctorConclusion; // JSON for form data (SURGERY: bloodPressure, signedConsent; ORTHO: trayProgress; etc.)
        private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
        private List<String> imageUrls;
    }
}
