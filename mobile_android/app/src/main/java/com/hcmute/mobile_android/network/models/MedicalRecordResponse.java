package com.hcmute.mobile_android.network.models;

import java.util.List;

public class MedicalRecordResponse {
    private Long id;
    private String date;
    private String diagnosis;
    private String doctorName;
    private String doctorSpecialty;
    private String symptoms;
    private String advice;
    private Prescription prescription;

    public Long getId() { return id; }
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getSymptoms() { return symptoms; }
    public String getAdvice() { return advice; }
    public Prescription getPrescription() { return prescription; }

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
}
