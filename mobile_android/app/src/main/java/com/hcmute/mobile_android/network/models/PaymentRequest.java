package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class PaymentRequest {
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("amount")
    private BigDecimal amount;
    
    @SerializedName("note")
    private String note;
    
    public PaymentRequest(String paymentMethod, BigDecimal amount, String note) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.note = note;
    }
    
    // Getters and setters
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
