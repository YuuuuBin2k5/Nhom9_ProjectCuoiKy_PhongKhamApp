package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO yêu cầu tạo mã QR phục vụ quy trình Check-in (UC_06).
 */
@Data
public class GenerateCheckInQRRequest {
    private Long appointmentId;
    private Long patientId;
    private String patientPhone;
}
