package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO chứa thông tin phản hồi về hồ sơ bệnh án của bệnh nhân.
 */
public class MedicalRecordResponse {
    /** ID của hồ sơ bệnh án */
    private Long id;
    /** ID của cuộc hẹn liên quan */
    private Long appointmentId;
    /** Ngày khám */
    private String date;
    /** Chẩn đoán của bác sĩ */
    private String diagnosis;
    /** Tên bác sĩ thực hiện */
    private String doctorName;
    /** Chuyên khoa của bác sĩ */
    private String doctorSpecialty;
    /** Triệu chứng của bệnh nhân */
    private String symptoms;
    /** Lời khuyên của bác sĩ */
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
        private String frequency;
        private String duration;
        private String unit;
        private Integer quantity;

        public String getMedicineName() { return medicineName; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
        public String getUnit() { return unit; }
        public Integer getQuantity() { return quantity; }
    }
    
    // NEW: Treatment step detail for step-by-step display
    public static class TreatmentStepDetail {
        private String serviceName;
        private String toothNumber;
        private String notes;
        private String completedAt;
        private List<String> imageUrls;
        
        public String getServiceName() { return serviceName; }
        public String getToothNumber() { return toothNumber; }
        public String getNotes() { return notes; }
        public String getCompletedAt() { return completedAt; }
        public List<String> getImageUrls() { return imageUrls; }
    }
}
