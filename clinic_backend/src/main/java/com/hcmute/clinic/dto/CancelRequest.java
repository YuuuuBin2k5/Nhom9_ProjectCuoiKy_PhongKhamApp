package com.hcmute.clinic.dto;

import lombok.*;

/**
 * DTO chứa thông tin yêu cầu hủy lịch hẹn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {
    private String reason;
}
