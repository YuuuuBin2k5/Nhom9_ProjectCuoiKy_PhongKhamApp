package com.hcmute.mobile_android.network.models;

import java.util.List;

public class MedicalRecordDetailResponse {
    private Long id;
    private String date;
    private String diagnosis;
    private String doctorName;
    private String doctorSpecialty;
    private String symptoms;
    private String bloodPressure;
    private Integer heartRate;
    private String advice;
    private PrescriptionResponse prescription;
    private List<Detail> details;
    private List<TreatmentStepDetail> treatmentSteps;

    public Long getId() { return id; }
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getSymptoms() { return symptoms; }
    public String getBloodPressure() { return bloodPressure; }
    public Integer getHeartRate() { return heartRate; }
    public String getAdvice() { return advice; }
    public PrescriptionResponse getPrescription() { return prescription; }
    public List<Detail> getDetails() { return details; }
    public List<TreatmentStepDetail> getTreatmentSteps() { return treatmentSteps; }

    public static class Detail {
        private String serviceName;
        private String toothNumber;
        private String note;

        public String getServiceName() { return serviceName; }
        public String getToothNumber() { return toothNumber; }
        public String getNote() { return note; }
    }

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

    public static class PrescriptionResponse {
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
}
