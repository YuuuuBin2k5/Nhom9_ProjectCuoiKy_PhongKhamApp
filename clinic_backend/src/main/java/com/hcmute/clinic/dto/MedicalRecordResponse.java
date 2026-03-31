package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
}
