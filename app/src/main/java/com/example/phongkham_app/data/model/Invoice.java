package com.example.phongkham_app.data.model;

public class Invoice {
    private String code;
    private String patientName;
    private String doctorName;
    private String serviceName;
    private String date;
    private String amount;

    public Invoice() {
    }

    public Invoice(String code, String patientName, String doctorName, String serviceName, String date, String amount) {
        this.code = code;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.serviceName = serviceName;
        this.date = date;
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
