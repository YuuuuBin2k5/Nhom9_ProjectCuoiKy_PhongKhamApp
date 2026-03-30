package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorDetailDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String roomName;
    private Integer experienceYears;
    private String biography;
    private String avatarUrl;
    private long appointmentCount;
}
