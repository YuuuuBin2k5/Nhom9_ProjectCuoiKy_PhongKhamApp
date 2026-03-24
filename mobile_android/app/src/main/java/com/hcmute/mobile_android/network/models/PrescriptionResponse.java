package com.hcmute.mobile_android.network.models;

import java.util.List;

public class PrescriptionResponse {
    private Long id;
    private String doctorName;
    private String date;
    private List<PrescriptionDetail> details;

    public Long getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public List<PrescriptionDetail> getDetails() { return details; }

    public static class PrescriptionDetail {
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
}
