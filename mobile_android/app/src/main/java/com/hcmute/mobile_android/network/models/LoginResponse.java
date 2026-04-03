package com.hcmute.mobile_android.network.models;

/**
 * DTO chứa thông tin phản hồi sau khi đăng nhập thành công, bao gồm token xác thực.
 */
public class LoginResponse {
    /** Token truy cập dùng để xác thực các yêu cầu API. */
    private String token;
    /** Email của người dùng. */
    private String email;
    /** Họ và tên của người dùng. */
    private String fullName;
    /** Vai trò của người dùng trong hệ thống. */
    private String role;
    /** ID định danh của người dùng. */
    private Long userId;
    /** Token dùng để làm mới phiên đăng nhập. */
    private String refreshToken;

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
    public String getRefreshToken() { return refreshToken; }
}
