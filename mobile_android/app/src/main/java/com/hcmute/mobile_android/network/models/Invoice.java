package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Invoice {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("patientId")
    private Long patientId;
    
    @SerializedName("patientName")
    private String patientName;
    
    @SerializedName("totalAmount")
    private BigDecimal totalAmount;
    
    @SerializedName("paymentStatus")
    private String paymentStatus;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("paidAt")
    private Date paidAt;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    @SerializedName("items")
    private List<InvoiceItem> items;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    
    public static class InvoiceItem {
        @SerializedName("serviceName")
        private String serviceName;
        
        @SerializedName("price")
        private BigDecimal price;
        
        @SerializedName("quantity")
        private Integer quantity;
        
        @SerializedName("subtotal")
        private BigDecimal subtotal;
        
        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
