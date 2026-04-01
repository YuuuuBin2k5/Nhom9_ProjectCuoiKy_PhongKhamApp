package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class AdminRevenueCategory {
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("totalAmount")
    private BigDecimal totalAmount;

    public String getCategoryName() { return categoryName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
