package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDto {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private boolean fromPatient;
    private String content;
    private String createdAt;
}
