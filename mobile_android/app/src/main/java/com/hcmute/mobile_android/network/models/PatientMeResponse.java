package com.hcmute.mobile_android.network.models;

public class PatientMeResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String qrCodeData;
    private String phone;
    private String address;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getQrCodeData() { return qrCodeData; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}
