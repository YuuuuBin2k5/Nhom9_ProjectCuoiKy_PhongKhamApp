package com.hcmute.mobile_android.network.models;

import java.util.List;

public class PrescriptionResponse {
    private Long id;
    private Long medicalRecordId;
    private Long doctorId;
    private String doctorName;
    private List<DetailResponse> details;

    public static class DetailResponse {
        private Long id;
        private String medicineName;
        private String dosage;
        private String frequency;
        private String duration;
        private String unit;

        public String getMedicineName() { return medicineName; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
        public String getUnit() { return unit; }
    }

    public List<DetailResponse> getDetails() { return details; }
}
