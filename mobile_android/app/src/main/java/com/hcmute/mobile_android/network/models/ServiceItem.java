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
    @SerializedName("categoryId")
    private Integer categoryId;
    @SerializedName("categoryName")
    private String categoryName;
    @SerializedName("imageUrls")
    private java.util.List<String> imageUrls;
    @SerializedName("active")
    private boolean active;

    public ServiceItem() {
    }

    public ServiceItem(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getUiTemplateType() { return uiTemplateType; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public java.util.List<String> getImageUrls() { return imageUrls; }
    public boolean isActive() { return active; }
}
