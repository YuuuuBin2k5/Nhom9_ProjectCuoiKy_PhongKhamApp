package com.hcmute.mobile_android.network.models;

public class PatientMeResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String qrCodeData;
    private String phone;
    private String address;
    private String gender;
    private String dob;
    private String avatarUrl;
    private String allergies;
    private String underlyingConditions;
    private String bloodType;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getQrCodeData() { return qrCodeData; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getGender() { return gender; }
    public String getDob() { return dob; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getAllergies() { return allergies; }
    public String getUnderlyingConditions() { return underlyingConditions; }
    public String getBloodType() { return bloodType; }
}

