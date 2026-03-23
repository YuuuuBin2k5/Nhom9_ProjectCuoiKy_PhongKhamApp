package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class CreateAppointmentRequest {
    @SerializedName("service_id")
    private Long serviceId;

    @SerializedName("doctor_id")
    private Long doctorId;

    @SerializedName("patient_id")
    private Long patientId;

    @SerializedName("appointment_datetime")
    private String appointmentDatetime;

    @SerializedName("booking_type")
    private String bookingType;

    @SerializedName("status")
    private String status;

    public CreateAppointmentRequest(Long serviceId, Long doctorId, Long patientId, String appointmentDatetime) {
        this.serviceId = serviceId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentDatetime = appointmentDatetime;
        this.bookingType = "WALK_IN";
        this.status = "SCHEDULED";
    }

    public Long getServiceId() { return serviceId; }
    public Long getDoctorId() { return doctorId; }
    public Long getPatientId() { return patientId; }
    public String getAppointmentDatetime() { return appointmentDatetime; }
    public String getBookingType() { return bookingType; }
    public String getStatus() { return status; }
}
