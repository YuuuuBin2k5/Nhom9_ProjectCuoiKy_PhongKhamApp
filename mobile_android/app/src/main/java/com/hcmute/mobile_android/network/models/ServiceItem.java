package com.hcmute.mobile_android.network.models;

import com.google.gson.annotations.SerializedName;

public class ServiceItem {
    @SerializedName("id")
    private Long id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("price")
    private double price;
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    @SerializedName("uiTemplateType")
    private String uiTemplateType;
    @SerializedName("categoryName")
    private String categoryName;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getUiTemplateType() { return uiTemplateType; }
    public String getCategoryName() { return categoryName; }
}
