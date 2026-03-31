package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private String date;
    private String diagnosis;
    private String doctorName;
    private String doctorSpecialty;
    private String symptoms;
    private String advice;
    
    // Support both String and Object for prescription
    @SerializedName("prescription")
    private Object prescriptionRaw;
    
    private List<String> services;
    private String totalAmount;
    private String paymentStatus;
    
    // NEW: Treatment step details for step-by-step view
    private List<TreatmentStepDetail> treatmentSteps;

    public Long getId() { return id; }
    public Long getAppointmentId() { return appointmentId; }
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getSymptoms() { return symptoms; }
    public String getAdvice() { return advice; }
    public List<String> getServices() { return services; }
    public String getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public List<TreatmentStepDetail> getTreatmentSteps() { return treatmentSteps; }
    
    // Smart getter for prescription
    public String getPrescriptionText() {
        if (prescriptionRaw instanceof String) {
            return (String) prescriptionRaw;
        } else if (prescriptionRaw instanceof Prescription) {
            Prescription p = (Prescription) prescriptionRaw;
            if (p.getDetails() != null && !p.getDetails().isEmpty()) {
                return p.getDetails().size() + " loại thuốc";
            }
        }
        return "Không có đơn thuốc";
    }
    
    public Prescription getPrescription() {
        if (prescriptionRaw instanceof Prescription) {
            return (Prescription) prescriptionRaw;
        }
        return null;
    }

    public static class Prescription {
        private Long id;
        private List<PrescriptionDetail> details;

        public Long getId() { return id; }
        public List<PrescriptionDetail> getDetails() { return details; }
    }

    public static class PrescriptionDetail {
        private String medicineName;
        private String dosage;
        private Integer quantity;

        public String getMedicineName() { return medicineName; }
        public String getDosage() { return dosage; }
        public Integer getQuantity() { return quantity; }
    }
    
    // NEW: Treatment step detail for step-by-step display
    public static class TreatmentStepDetail {
        private String serviceName;
        private String toothNumber;
        private String notes;
        private String completedAt;
        
        public String getServiceName() { return serviceName; }
        public String getToothNumber() { return toothNumber; }
        public String getNotes() { return notes; }
        public String getCompletedAt() { return completedAt; }
    }
}
