package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class RevenueReport {
    @SerializedName("year")
    private Integer year;
    
    @SerializedName("month")
    private Integer month;
    
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("totalAppointments")
    private Integer totalAppointments;
    
    @SerializedName("completedAppointments")
    private Integer completedAppointments;
    
    @SerializedName("cancelledAppointments")
    private Integer cancelledAppointments;
    
    @SerializedName("averageRevenuePerAppointment")
    private BigDecimal averageRevenuePerAppointment;
    
    // Getters and setters
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Integer totalAppointments) { this.totalAppointments = totalAppointments; }
    
    public Integer getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(Integer completedAppointments) { this.completedAppointments = completedAppointments; }
    
    public Integer getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(Integer cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }
    
    public BigDecimal getAverageRevenuePerAppointment() { return averageRevenuePerAppointment; }
    public void setAverageRevenuePerAppointment(BigDecimal averageRevenuePerAppointment) { 
        this.averageRevenuePerAppointment = averageRevenuePerAppointment; 
    }
}
