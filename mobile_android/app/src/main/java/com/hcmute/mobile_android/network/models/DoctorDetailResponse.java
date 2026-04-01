package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class DoctorDetailResponse {
    @SerializedName("id") private Long id;
    @SerializedName("firstName") private String firstName;
    @SerializedName("lastName") private String lastName;
    @SerializedName("specialization") private String specialization;
    @SerializedName("roomName") private String roomName;
    @SerializedName("experienceYears") private Integer experienceYears;
    @SerializedName("biography") private String biography;
    @SerializedName("avatarUrl") private String avatarUrl;
    @SerializedName("appointmentCount") private long appointmentCount;

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
    public String getRoomName() { return roomName; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getBiography() { return biography; }
    public String getAvatarUrl() { return avatarUrl; }
    public long getAppointmentCount() { return appointmentCount; }

    public String getDisplayName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        String n = (fn + " " + ln).trim();
        return n.isEmpty() ? "Bác sĩ" : ("BS. " + n);
    }
}
