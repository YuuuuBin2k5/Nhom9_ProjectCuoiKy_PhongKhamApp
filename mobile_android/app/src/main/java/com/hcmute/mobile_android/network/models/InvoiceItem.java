package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class InvoiceItem {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("toothNumber")
    private String toothNumber;
    
    @SerializedName("quantity")
    private Integer quantity;
    
    @SerializedName("unitPrice")
    private Double unitPrice;
    
    @SerializedName("totalPrice")
    private Double totalPrice;
    
    @SerializedName("description")
    private String description;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getToothNumber() { return toothNumber; }
    public void setToothNumber(String toothNumber) { this.toothNumber = toothNumber; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
