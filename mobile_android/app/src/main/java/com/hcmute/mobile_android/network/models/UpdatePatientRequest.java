package com.hcmute.mobile_android.network.models;

public class UpdatePatientRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String gender;
    private String dob;
    private String avatarUrl;
    private String allergies;
    private String underlyingConditions;
    private String bloodType;

    public UpdatePatientRequest(String firstName, String lastName, String phone, String address, String gender, String dob, String avatarUrl, String allergies, String underlyingConditions, String bloodType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.dob = dob;
        this.avatarUrl = avatarUrl;
        this.allergies = allergies;
        this.underlyingConditions = underlyingConditions;
        this.bloodType = bloodType;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getGender() { return gender; }
    public String getDob() { return dob; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getAllergies() { return allergies; }
    public String getUnderlyingConditions() { return underlyingConditions; }
    public String getBloodType() { return bloodType; }
}
