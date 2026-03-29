package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class RevenueReport {
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("averageRevenuePerAppointment")
    private BigDecimal averageRevenuePerAppointment;
    
    // Getters and setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Integer totalAppointments) { this.totalAppointments = totalAppointments; }
    
    public BigDecimal getAverageRevenuePerAppointment() { return averageRevenuePerAppointment; }
    public void setAverageRevenuePerAppointment(BigDecimal averageRevenuePerAppointment) { 
        this.averageRevenuePerAppointment = averageRevenuePerAppointment; 
    }
}
