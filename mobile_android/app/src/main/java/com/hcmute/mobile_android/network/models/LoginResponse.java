package com.hcmute.mobile_android.network.models;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private Long userId;

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}
