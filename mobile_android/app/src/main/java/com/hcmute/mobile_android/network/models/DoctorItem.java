package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class DoctorItem {
    @SerializedName("id")
    private Long id;
    @SerializedName("firstName")
    private String firstName;
    @SerializedName("lastName")
    private String lastName;
    @SerializedName("email")
    private String email;
    @SerializedName("specialization")
    private String specialization;
    @SerializedName("roomName")
    private String roomName;
    @SerializedName("experienceYears")
    private Integer experienceYears;
    @SerializedName("appointmentCount")
    private int appointmentCount;
<<<<<<< HEAD
    @SerializedName("active")
    private boolean isActive;
    @SerializedName("avatarUrl")
    private String avatarUrl;
=======
    @SerializedName("isSpecialist")
    private boolean specialist;
    @SerializedName("avatarUrl")
    private String avatarUrl;
    @SerializedName("active")
    private boolean active;
>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public String getRoomName() { return roomName; }
    public Integer getExperienceYears() { return experienceYears; }
    public int getAppointmentCount() { return appointmentCount; }
<<<<<<< HEAD
    public boolean isActive() { return isActive; }
    public String getAvatarUrl() { return avatarUrl; }

    public void setActive(boolean active) { isActive = active; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
=======
    public boolean isSpecialist() { return specialist; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
>>>>>>> 492f872343b2ce06255b5595414c8b8dfe77b756

    public String getSpecialty() { return specialization; }

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }
}
