package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SavePrescriptionRequest {
    @SerializedName("medicines")
    private List<MedicineItem> medicines;

    @SerializedName("monitoringDays")
    private Integer monitoringDays;

    @SerializedName("monitoringStartDate")
    private String monitoringStartDate; // "yyyy-MM-dd"

    public SavePrescriptionRequest() {}

    public SavePrescriptionRequest(List<MedicineItem> medicines, Integer monitoringDays, String monitoringStartDate) {
        this.medicines = medicines;
        this.monitoringDays = monitoringDays;
        this.monitoringStartDate = monitoringStartDate;
    }

    public List<MedicineItem> getMedicines() { return medicines; }
    public void setMedicines(List<MedicineItem> medicines) { this.medicines = medicines; }
    public Integer getMonitoringDays() { return monitoringDays; }
    public void setMonitoringDays(Integer monitoringDays) { this.monitoringDays = monitoringDays; }
    public String getMonitoringStartDate() { return monitoringStartDate; }
    public void setMonitoringStartDate(String monitoringStartDate) { this.monitoringStartDate = monitoringStartDate; }
}
