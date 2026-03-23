package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientMeResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String qrCodeData;
}
