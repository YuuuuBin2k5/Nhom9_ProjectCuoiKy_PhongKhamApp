package com.example.phongkham_app.data.model;

public class Appointment {
    private String id;
    private String patientName;
    private String doctorName;
    private String time;
    private String date;
    private String status;

    public Appointment() {
    }

    public Appointment(String patientName, String time) {
        this.patientName = patientName;
        this.time = time;
    }

    public Appointment(String id, String patientName, String doctorName, String time, String date, String status) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.time = time;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
