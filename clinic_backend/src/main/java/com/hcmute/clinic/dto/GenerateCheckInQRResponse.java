package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO phản hồi chứa dữ liệu mã QR Check-in đã được khởi tạo.
 */
@Data
@Builder
public class GenerateCheckInQRResponse {
    private String qrData;
    private String displayCode;
    private String patientName;
    private Long appointmentId;
    private String expiresAt;
}
