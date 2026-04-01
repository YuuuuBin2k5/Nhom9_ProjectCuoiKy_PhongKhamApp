package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for adding a service to a specific tooth
 * Sent to backend when doctor selects a service for a tooth
 */
public class AddToothServiceRequest {
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("sequenceOrder")
    private Integer sequenceOrder;
    
    public AddToothServiceRequest() {
    }
    
    public AddToothServiceRequest(Long serviceId, Integer sequenceOrder) {
        this.serviceId = serviceId;
        this.sequenceOrder = sequenceOrder;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public Integer getSequenceOrder() {
        return sequenceOrder;
    }
    
    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}
