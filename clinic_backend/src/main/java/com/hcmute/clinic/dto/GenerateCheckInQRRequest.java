package com.hcmute.clinic.dto;

import lombok.Data;

@Data
public class GenerateCheckInQRRequest {
    private Long appointmentId;
    private Long patientId;
    private String patientPhone;
}
