package com.hcmute.clinic.dto;

import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu đăng nhập từ người dùng.
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
