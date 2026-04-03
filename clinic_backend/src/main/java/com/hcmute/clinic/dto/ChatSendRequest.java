package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa nội dung tin nhắn cần gửi đi.
 */
@Data
public class ChatSendRequest {
    private String content;
}
