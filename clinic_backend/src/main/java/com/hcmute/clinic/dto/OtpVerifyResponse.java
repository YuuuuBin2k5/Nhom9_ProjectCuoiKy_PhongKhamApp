package com.hcmute.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyResponse {
    /** JWT khi đăng nhập thành công; null nếu cần đăng ký */
    private String token;
    private String email;
    private String role;
    /** true khi purpose LOGIN nhưng chưa có Patient với SĐT này */
    private boolean needsRegistration;
    private Long userId;
}
