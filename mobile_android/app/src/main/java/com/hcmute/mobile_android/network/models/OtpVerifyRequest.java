package com.hcmute.mobile_android.network.models;

public class OtpVerifyRequest {
    private final String phone;
    private final String code;
    private final String purpose;

    public OtpVerifyRequest(String phone, String code, String purpose) {
        this.phone = phone;
        this.code = code;
        this.purpose = purpose;
    }

    public String getPhone() {
        return phone;
    }

    public String getCode() {
        return code;
    }

    public String getPurpose() {
        return purpose;
    }
}
