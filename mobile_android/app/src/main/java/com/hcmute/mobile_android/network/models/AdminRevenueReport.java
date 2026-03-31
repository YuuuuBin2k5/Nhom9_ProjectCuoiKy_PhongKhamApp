package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class AdminRevenueReport {
    @SerializedName("totalRevenue")
    private BigDecimal totalRevenue;
    
    @SerializedName("categories")
    private List<AdminRevenueCategory> categories;

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public List<AdminRevenueCategory> getCategories() { return categories; }
}
