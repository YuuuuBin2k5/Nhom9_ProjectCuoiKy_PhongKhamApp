package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class DoctorStats {
    @SerializedName("doctorName")
    private String doctorName;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("completedAppointments")
    private Integer completedAppointments;
    
    @SerializedName("averageRating")
    private Double averageRating;
    
    // Getters and setters
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public Integer getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Integer totalAppointments) { this.totalAppointments = totalAppointments; }
    
    public Integer getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(Integer completedAppointments) { 
        this.completedAppointments = completedAppointments; 
    }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
}
