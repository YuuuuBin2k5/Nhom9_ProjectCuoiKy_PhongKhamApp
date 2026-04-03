package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO mô tả toàn bộ phác đồ điều trị của bệnh nhân.
 * Chứa danh sách các bước điều trị (UC_13/14) và trạng thái hoàn thành.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanDTO {
    private Long id;
    private Long patientId;
    private Long appointmentId; // ADDED: To validate prescription belongs to correct appointment
    private String status;
    private boolean isDraft;
    private Long prescriptionId;
    private String diagnosis;
    private String advice;
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
        private String doctorName;
        private String description;
        private Integer stepOrder;
        private String status;
        private String toothNumber;
        private Double estimatedPrice;
        private Double actualPrice;
        private String doctorConclusion;
        private String completedAt;
        private String uiTemplateType;
        private String roomName;
        private String medicationDetails;
        private Long prescriptionId;
        private String diagnosis;
        private String advice;
        private List<PrescriptionDTO.DetailDTO> prescriptionDetails;
        private List<String> imageUrls;
        private boolean editable;
    }
}
