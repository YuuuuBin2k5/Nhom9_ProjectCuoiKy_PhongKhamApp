package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class OtpVerifyResponse {
    private String token;
    private String email;
    private String role;

    @SerializedName("needsRegistration")
    private boolean needsRegistration;

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isNeedsRegistration() {
        return needsRegistration;
    }
}
