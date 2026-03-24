package com.hcmute.clinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {
    @JsonProperty("service_id")
    private Long serviceId;

    @JsonProperty("doctor_id")
    private Long doctorId;

    @JsonProperty("patient_id")
    private Long patientId;

    @JsonProperty("appointment_datetime")
    private String appointmentDatetime; // ISO format or "yyyy-MM-dd HH:mm:ss"

    @JsonProperty("booking_type")
    private String bookingType; // e.g., "WALK_IN", "ONLINE"

    @JsonProperty("status")
    private String status; // e.g., "SCHEDULED"
}
