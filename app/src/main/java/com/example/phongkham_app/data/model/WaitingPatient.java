package com.example.phongkham_app.data.model;

public class WaitingPatient {
    private String id;
    private String name;
    private String time;
    private String status;

    public WaitingPatient() {
    }

    public WaitingPatient(String name, String time) {
        this.name = name;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
