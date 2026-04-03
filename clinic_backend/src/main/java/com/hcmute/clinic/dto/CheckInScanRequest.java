package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa dữ liệu mã QR từ yêu cầu quét Check-in.
 */
@Data
public class CheckInScanRequest {
    private String qrData;
}
