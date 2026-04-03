package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi thông tin hồ sơ bệnh án chi tiết.
 * Tổng hợp dữ liệu từ chẩn đoán, dịch vụ thực hiện đến đơn thuốc (UC_07).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private String date;
    private String doctorName;
    private String doctorSpecialty;
    private String diagnosis;
    private String symptoms;
    private String advice;
    private String prescription;
    private List<String> services;
    private String totalAmount;
    private String paymentStatus;
    
    // NEW: Treatment step details for step-by-step view
    private List<TreatmentStepDetail> treatmentSteps;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreatmentStepDetail {
        private String serviceName;
        private String toothNumber;
        private String notes;
        private String completedAt;
    }
}
