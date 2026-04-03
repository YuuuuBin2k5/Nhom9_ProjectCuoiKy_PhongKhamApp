package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * DTO đại diện cho thông tin hóa đơn thanh toán của bệnh nhân.
 */
public class Invoice {
    /** ID của hóa đơn */
    @SerializedName("id")
    private Long id;
    
    /** ID của bệnh nhân */
    @SerializedName("patientId")
    private Long patientId;
    
    /** Tên của bệnh nhân */
    @SerializedName("patientName")
    private String patientName;
    
    @SerializedName("treatmentPlanId")
    private Long treatmentPlanId;
    
    @SerializedName("totalAmount")
    private BigDecimal totalAmount;
    
    @SerializedName("paymentStatus")
    private String paymentStatus;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("paidAt")
    private String paidAt;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("prescriptionId")
    private Long prescriptionId;
    
    @SerializedName("diagnosis")
    private String diagnosis;
    
    @SerializedName("advice")
    private String advice;

    @SerializedName("items")
    private List<InvoiceItem> items;
    
    @SerializedName("prescriptionDetails")
    private List<PrescriptionDetail> prescriptionDetails;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public Long getTreatmentPlanId() { return treatmentPlanId; }
    public void setTreatmentPlanId(Long treatmentPlanId) { this.treatmentPlanId = treatmentPlanId; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public Long getPrescriptionId() { return prescriptionId; }
    public String getDiagnosis() { return diagnosis; }
    public String getAdvice() { return advice; }
    
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    
    public List<PrescriptionDetail> getPrescriptionDetails() { return prescriptionDetails; }
    public void setPrescriptionDetails(List<PrescriptionDetail> prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }
    
    public static class InvoiceItem {
        @SerializedName("id")
        private Long id;
        
        @SerializedName("serviceName")
        private String serviceName;
        
        @SerializedName("toothNumber")
        private String toothNumber;
        
        @SerializedName("quantity")
        private Integer quantity;
        
        @SerializedName("unitPrice")
        private BigDecimal unitPrice;
        
        @SerializedName("totalPrice")
        private BigDecimal totalPrice;
        
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
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class PrescriptionDetail {
        @SerializedName("id")
        private Long id;
        @SerializedName("medicineName")
        private String medicineName;
        @SerializedName("dosage")
        private String dosage;
        @SerializedName("frequency")
        private String frequency;
        @SerializedName("duration")
        private String duration;
        @SerializedName("unit")
        private String unit;

        public Long getId() { return id; }
        public String getMedicineName() { return medicineName; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
        public String getUnit() { return unit; }
    }
}
