package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class DoctorStats {
    @SerializedName("doctorId")
    private Long doctorId;
    
    @SerializedName("doctorName")
    private String doctorName;
    
    @SerializedName("specialization")
    private String specialization;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("completedAppointments")
    private Integer completedAppointments;
    
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("averageRating")
    private BigDecimal averageRating;
    
    @SerializedName("totalReviews")
    private Integer totalReviews;
    
    // Getters and setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public Integer getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Integer totalAppointments) { this.totalAppointments = totalAppointments; }
    
    public Integer getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(Integer completedAppointments) { 
        this.completedAppointments = completedAppointments; 
    }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}
