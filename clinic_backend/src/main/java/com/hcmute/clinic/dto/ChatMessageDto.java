package com.hcmute.clinic.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO đại diện cho nội dung một tin nhắn trong hệ thống Chat.
 */
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
