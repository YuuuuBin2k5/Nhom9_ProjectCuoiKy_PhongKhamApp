package com.hcmute.mobile_android.network.models;

public class RegisterResultResponse {
    private String message;
    private String token;
    private String email;
    private String role;

    private String refreshToken;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
