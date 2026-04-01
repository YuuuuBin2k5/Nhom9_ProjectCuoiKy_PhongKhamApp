package com.hcmute.mobile_android.network.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request model for adding a service to multiple teeth at once
 * Used for services like crown, extraction that can be applied to multiple teeth
 */
public class AddMultipleTeethServiceRequest {
    private Long serviceId;
    private List<String> toothNumbers;
    private Integer startingSequenceOrder;
    private String notes;
    private BigDecimal customPrice;

    public AddMultipleTeethServiceRequest() {
    }

    public AddMultipleTeethServiceRequest(Long serviceId, List<String> toothNumbers, 
                                         Integer startingSequenceOrder, String notes, 
                                         BigDecimal customPrice) {
        this.serviceId = serviceId;
        this.toothNumbers = toothNumbers;
        this.startingSequenceOrder = startingSequenceOrder;
        this.notes = notes;
        this.customPrice = customPrice;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getToothNumbers() {
        return toothNumbers;
    }

    public void setToothNumbers(List<String> toothNumbers) {
        this.toothNumbers = toothNumbers;
    }

    public Integer getStartingSequenceOrder() {
        return startingSequenceOrder;
    }

    public void setStartingSequenceOrder(Integer startingSequenceOrder) {
        this.startingSequenceOrder = startingSequenceOrder;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getCustomPrice() {
        return customPrice;
    }

    public void setCustomPrice(BigDecimal customPrice) {
        this.customPrice = customPrice;
    }
}
