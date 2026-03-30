package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * Response model when a general service is added
 * Received from backend after adding general service
 */
public class GeneralServiceResponse {
    @SerializedName("stepId")
    private Long stepId;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("totalPlanCost")
    private BigDecimal totalPlanCost;
    
    public GeneralServiceResponse() {
    }
    
    public GeneralServiceResponse(Long stepId, String serviceName, BigDecimal price, BigDecimal totalPlanCost) {
        this.stepId = stepId;
        this.serviceName = serviceName;
        this.price = price;
        this.totalPlanCost = totalPlanCost;
    }
    
    public Long getStepId() {
        return stepId;
    }
    
    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getTotalPlanCost() {
        return totalPlanCost;
    }
    
    public void setTotalPlanCost(BigDecimal totalPlanCost) {
        this.totalPlanCost = totalPlanCost;
    }
}
