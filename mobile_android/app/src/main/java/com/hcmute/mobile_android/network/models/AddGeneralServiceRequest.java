package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for adding a general service
 * Sent to backend when doctor adds a general service (not specific to a tooth)
 */
public class AddGeneralServiceRequest {
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("sequenceOrder")
    private Integer sequenceOrder;
    
    public AddGeneralServiceRequest() {
    }
    
    public AddGeneralServiceRequest(Long serviceId, Integer sequenceOrder) {
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
