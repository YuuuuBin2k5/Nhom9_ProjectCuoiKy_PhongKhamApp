package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class MedicineItem {
    @SerializedName("medicineName")
    private String medicineName;

    @SerializedName("dosage")
    private String dosage;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("duration")
    private String duration;

    @SerializedName("unit")
    private String unit;

    @SerializedName("price")
    private Double price;

    public MedicineItem() {}

    public MedicineItem(String medicineName, String dosage, String frequency, String duration, String unit, Double price) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.unit = unit;
        this.price = price;
    }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
