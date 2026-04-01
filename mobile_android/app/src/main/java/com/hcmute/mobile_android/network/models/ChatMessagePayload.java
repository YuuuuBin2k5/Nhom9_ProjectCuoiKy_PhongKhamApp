package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessagePayload {
    @SerializedName("id") private Long id;
    @SerializedName("patientId") private Long patientId;
    @SerializedName("doctorId") private Long doctorId;
    @SerializedName("fromPatient") private boolean fromPatient;
    @SerializedName("content") private String content;
    @SerializedName("createdAt") private String createdAt;

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public boolean isFromPatient() { return fromPatient; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
}
