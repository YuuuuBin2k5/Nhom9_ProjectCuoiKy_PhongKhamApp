package com.example.phongkham_app.data.model;

public class VisitHistory {
    private int id;
    private String date;
    private String visitType;
    private String doctorName;
    private String status;

    public VisitHistory() {
    }

    public VisitHistory(int id, String date, String visitType, String doctorName, String status) {
        this.id = id;
        this.date = date;
        this.visitType = visitType;
        this.doctorName = doctorName;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
