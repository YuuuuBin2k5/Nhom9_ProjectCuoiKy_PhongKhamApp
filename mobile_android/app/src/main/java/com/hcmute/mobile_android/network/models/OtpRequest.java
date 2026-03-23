package com.hcmute.mobile_android.network.models;

public class OtpRequest {
    private final String phone;
    private final String purpose;

    public OtpRequest(String phone, String purpose) {
        this.phone = phone;
        this.purpose = purpose;
    }

    public String getPhone() {
        return phone;
    }

    public String getPurpose() {
        return purpose;
    }
}
