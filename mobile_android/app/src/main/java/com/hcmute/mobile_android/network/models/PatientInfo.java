package com.hcmute.mobile_android.network.models;

import java.io.Serializable;

/**
 * DTO đại diện cho thông tin chi tiết và hồ sơ bệnh nhân.
 */
public class PatientInfo implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String address;
    private Integer rewardPoints;
    private String qrCodeData;
    private String bookedService;
    private String appointmentStatus;
    private Long queueId;
    private Long appointmentId;
    
    // FIX 1: Thêm fields mới cho TreatmentPlan
    private Long treatmentPlanId;
    private Boolean hasTreatmentPlan;
    private String treatmentPlanStatus;
    
    // NEW: Thêm fields cho PatientProfile
    private String bloodType;
    private String allergies;
    private String underlyingConditions;
    private String notes;

    // Constructors
    public PatientInfo() {}

    public String getBookedService() { return bookedService; }
    public void setBookedService(String bookedService) { this.bookedService = bookedService; }

    public String getAppointmentStatus() { return appointmentStatus; }
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }

    public Long getQueueId() { return queueId; }
    public void setQueueId(Long queueId) { this.queueId = queueId; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    // FIX 1: Getters and Setters cho TreatmentPlan fields
    public Long getTreatmentPlanId() { return treatmentPlanId; }
    public void setTreatmentPlanId(Long treatmentPlanId) { this.treatmentPlanId = treatmentPlanId; }

    public Boolean getHasTreatmentPlan() { return hasTreatmentPlan; }
    public void setHasTreatmentPlan(Boolean hasTreatmentPlan) { this.hasTreatmentPlan = hasTreatmentPlan; }

    public String getTreatmentPlanStatus() { return treatmentPlanStatus; }
    public void setTreatmentPlanStatus(String treatmentPlanStatus) { this.treatmentPlanStatus = treatmentPlanStatus; }
    
    // NEW: Getters and Setters cho PatientProfile fields
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    
    public String getUnderlyingConditions() { return underlyingConditions; }
    public void setUnderlyingConditions(String underlyingConditions) { this.underlyingConditions = underlyingConditions; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public PatientInfo(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    // Helper methods
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (lastName != null && !lastName.trim().isEmpty()) {
            name.append(lastName.trim());
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(firstName.trim());
        }
        return name.toString();
    }

    public String getGenderDisplay() {
        if (gender == null) return "Không xác định";
        switch (gender.toUpperCase()) {
            case "MALE": return "Nam";
            case "FEMALE": return "Nữ";
            case "OTHER": return "Khác";
            default: return gender;
        }
    }
    
    public String getBloodTypeDisplay() {
        if (bloodType == null || bloodType.trim().isEmpty()) {
            return "Chưa xác định";
        }
        return bloodType;
    }
    
    public String getAllergiesDisplay() {
        if (allergies == null || allergies.trim().isEmpty()) {
            return "Không có";
        }
        return allergies;
    }
    
    public String getUnderlyingConditionsDisplay() {
        if (underlyingConditions == null || underlyingConditions.trim().isEmpty()) {
            return "Không có";
        }
        return underlyingConditions;
    }
}