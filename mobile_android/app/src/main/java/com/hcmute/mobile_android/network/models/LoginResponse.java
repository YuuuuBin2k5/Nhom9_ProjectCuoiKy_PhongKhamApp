package com.hcmute.mobile_android.network.models;

public class LoginResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private Long userId;
    private String refreshToken;

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
    public String getRefreshToken() { return refreshToken; }
}
