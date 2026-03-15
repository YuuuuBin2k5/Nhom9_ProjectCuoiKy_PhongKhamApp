package com.example.phongkham_app.data.model;

public class Patient {
    private String id;
    private String name;
    private String bloodType;
    private int age;
    private String gender;
    private String status;
    private int imageResId;
    private String insuranceId;
    private String insuranceExpiry;

    public Patient() {
    }

    public Patient(String id, String name, String bloodType, int age, String gender, String status, int imageResId, String insuranceId, String insuranceExpiry) {
        this.id = id;
        this.name = name;
        this.bloodType = bloodType;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.imageResId = imageResId;
        this.insuranceId = insuranceId;
        this.insuranceExpiry = insuranceExpiry;
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

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getInsuranceExpiry() {
        return insuranceExpiry;
    }

    public void setInsuranceExpiry(String insuranceExpiry) {
        this.insuranceExpiry = insuranceExpiry;
    }
}
