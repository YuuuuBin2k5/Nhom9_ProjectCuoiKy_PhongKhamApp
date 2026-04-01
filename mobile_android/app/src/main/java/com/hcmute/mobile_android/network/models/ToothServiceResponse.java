package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * Response model when a service is added to a specific tooth
 * Received from backend after adding tooth-specific service
 */
public class ToothServiceResponse {
    @SerializedName("stepId")
    private Long stepId;
    
    @SerializedName("toothNumber")
    private String toothNumber;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("totalPlanCost")
    private BigDecimal totalPlanCost;
    
    public ToothServiceResponse() {
    }
    
    public ToothServiceResponse(Long stepId, String toothNumber, String serviceName, BigDecimal price, BigDecimal totalPlanCost) {
        this.stepId = stepId;
        this.toothNumber = toothNumber;
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
    
    public String getToothNumber() {
        return toothNumber;
    }
    
    public void setToothNumber(String toothNumber) {
        this.toothNumber = toothNumber;
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
