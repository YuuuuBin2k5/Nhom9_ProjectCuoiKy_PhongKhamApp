package com.hcmute.mobile_android.network.models;

/**
 * DTO chứa thông tin hồ sơ đầy đủ của bác sĩ.
 */
public class DoctorProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String licenseNumber;
    private Integer experienceYears;
    private String biography;
    private String avatarUrl;
    private String roomName;

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getBiography() { return biography; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRoomName() { return roomName; }

    public String getDisplayName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? "Bác sĩ" : full;
    }
}
