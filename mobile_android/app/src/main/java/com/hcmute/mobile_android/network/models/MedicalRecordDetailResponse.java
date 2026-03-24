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
    private Long prescriptionId;
    private List<Detail> details;

    public Long getId() { return id; }
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getSymptoms() { return symptoms; }
    public String getBloodPressure() { return bloodPressure; }
    public Integer getHeartRate() { return heartRate; }
    public String getAdvice() { return advice; }
    public Long getPrescriptionId() { return prescriptionId; }
    public List<Detail> getDetails() { return details; }

    public static class Detail {
        private String serviceName;
        private String toothNumber;
        private String note;

        public String getServiceName() { return serviceName; }
        public String getToothNumber() { return toothNumber; }
        public String getNote() { return note; }
    }
}
