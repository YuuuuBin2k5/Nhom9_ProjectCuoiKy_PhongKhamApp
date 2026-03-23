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

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
    public String getRoomName() { return roomName; }
    public Integer getExperienceYears() { return experienceYears; }
    public int getAppointmentCount() { return appointmentCount; }

    public String getSpecialty() { return specialization; }

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }
}
