package com.hcmute.mobile_android.network.models;

public class OtpRequest {
    private final String phone;
    private final String email;
    private final String purpose;

    public OtpRequest(String phone, String purpose) {
        this.phone = phone;
        this.email = null;
        this.purpose = purpose;
    }

    public OtpRequest(String phone, String email, String purpose) {
        this.phone = phone;
        this.email = email;
        this.purpose = purpose;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPurpose() {
        return purpose;
    }
}
