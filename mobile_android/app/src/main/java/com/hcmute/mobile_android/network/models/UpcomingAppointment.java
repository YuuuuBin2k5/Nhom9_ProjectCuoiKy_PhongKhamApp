package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class UpcomingAppointment {
    @SerializedName("id")
    private long id;

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("appointmentTime")
    private String appointmentTime;

    @SerializedName("serviceName")
    private String serviceName;

    @SerializedName("doctorName")
    private String doctorName;

    @SerializedName("status")
    private String status;

    public long getId() { return id; }
    public String getDatetime() { return datetime; }
    public String getAppointmentTime() { return appointmentTime != null ? appointmentTime : datetime; }
    public String getServiceName() { return serviceName; }
    public String getDoctorName() { return doctorName; }
    public String getStatus() { return status; }
}
