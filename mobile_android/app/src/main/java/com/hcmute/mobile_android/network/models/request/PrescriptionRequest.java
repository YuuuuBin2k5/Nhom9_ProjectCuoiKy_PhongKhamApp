package com.hcmute.mobile_android.network.models.request;

import java.util.List;

public class PrescriptionRequest {
    private Long appointmentId;
    private String diagnosis;
    private String symptoms;
    private String advice;
    private List<DetailRequest> details;

    public PrescriptionRequest(Long appointmentId, String diagnosis, String symptoms, String advice, List<DetailRequest> details) {
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.symptoms = symptoms;
        this.advice = advice;
        this.details = details;
    }

    public static class DetailRequest {
        private String medicineName;
        private String dosage;
        private String frequency;
        private String duration;
        private String unit;

        public DetailRequest(String medicineName, String dosage, String frequency, String duration, String unit) {
            this.medicineName = medicineName;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.unit = unit;
        }

        // Getters and Setters
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}
