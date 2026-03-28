package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Review {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("patientId")
    private Long patientId;
    
    @SerializedName("patientName")
    private String patientName;
    
    @SerializedName("doctorId")
    private Long doctorId;
    
    @SerializedName("doctorName")
    private String doctorName;
    
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("serviceName")
    private String serviceName;
    
    @SerializedName("appointmentId")
    private Long appointmentId;
    
    @SerializedName("rating")
    private Integer rating;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
