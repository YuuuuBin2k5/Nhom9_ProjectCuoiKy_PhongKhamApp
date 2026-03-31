package com.hcmute.mobile_android.network.models;

public class ResetPasswordRequest {
    private String phone;
    private String email;
    private String otp;
    private String newPassword;

    public ResetPasswordRequest(String phone, String email, String otp, String newPassword) {
        this.phone = phone;
        this.email = email;
        this.otp = otp;
        this.newPassword = newPassword;
    }

    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getOtp() { return otp; }
    public String getNewPassword() { return newPassword; }
}
