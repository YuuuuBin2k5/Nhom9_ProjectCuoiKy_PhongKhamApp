package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class ServiceStats {
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("totalBookings")
    private Integer totalBookings;
    
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("averageRating")
    private BigDecimal averageRating;
    
    @SerializedName("totalReviews")
    private Integer totalReviews;
    
    // Getters and setters
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public Integer getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}
