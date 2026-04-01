package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("appointmentId")
    private Long appointmentId;
    
    @SerializedName("doctorId")
    private Long doctorId;
    
    @SerializedName("serviceId")
    private Long serviceId;
    
    @SerializedName("rating")
    private Integer rating;
    
    @SerializedName("comment")
    private String comment;
    
    public ReviewRequest(Long appointmentId, Long doctorId, Long serviceId, 
                        Integer rating, String comment) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.serviceId = serviceId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and setters
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
