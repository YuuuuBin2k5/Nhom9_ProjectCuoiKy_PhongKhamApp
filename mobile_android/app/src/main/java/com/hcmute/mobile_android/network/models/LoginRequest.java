package com.hcmute.mobile_android.network.models;

/**
 * DTO chứa thông tin yêu cầu đăng nhập từ người dùng.
 * Gson cần getter để serialize JSON POST body ổn định trên mọi bản build.
 */
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
