package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrescriptionResponse {
    @SerializedName("hasPrescription")
    private Boolean hasPrescription;

    @SerializedName("prescriptionId")
    private Long prescriptionId;

    @SerializedName("medicines")
    private List<MedicineItem> medicines;

    @SerializedName("monitoringDays")
    private Integer monitoringDays;

    @SerializedName("monitoringStartDate")
    private String monitoringStartDate;

    @SerializedName("scheduledResumeDate")
    private String scheduledResumeDate;

    @SerializedName("defaultMonitoringDays")
    private Integer defaultMonitoringDays;

    @SerializedName("message")
    private String message;

    // Backward-compat fields (used by PrescriptionDetailActivity with appointment endpoint)
    @SerializedName("doctorName")
    private String doctorName;

    @SerializedName("date")
    private String date;

    public Boolean getHasPrescription() { return hasPrescription; }
    public Long getPrescriptionId() { return prescriptionId; }
    public List<MedicineItem> getMedicines() { return medicines; }
    /** Alias for getMedicines() for backward compatibility */
    public List<MedicineItem> getDetails() { return medicines; }
    public Integer getMonitoringDays() { return monitoringDays; }
    public String getMonitoringStartDate() { return monitoringStartDate; }
    public String getScheduledResumeDate() { return scheduledResumeDate; }
    public Integer getDefaultMonitoringDays() { return defaultMonitoringDays; }
    public String getMessage() { return message; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
}
