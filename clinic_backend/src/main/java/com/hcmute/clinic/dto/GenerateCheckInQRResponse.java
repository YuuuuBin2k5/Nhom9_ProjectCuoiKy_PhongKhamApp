package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateCheckInQRResponse {
    private String qrData;
    private String displayCode;
    private String patientName;
    private Long appointmentId;
    private String expiresAt;
}
